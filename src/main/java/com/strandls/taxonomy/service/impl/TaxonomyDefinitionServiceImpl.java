/**
 * 
 */
package com.strandls.taxonomy.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.strandls.taxonomy.dao.AcceptedSynonymDao;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.dao.TaxonomyRegistryDao;
import com.strandls.taxonomy.pojo.AcceptedSynonym;
import com.strandls.taxonomy.pojo.Rank;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.pojo.enumtype.TaxonomyPosition;
import com.strandls.taxonomy.pojo.enumtype.TaxonomyStatus;
import com.strandls.taxonomy.pojo.request.FileMetadata;
import com.strandls.taxonomy.pojo.request.TaxonomyCreationHierarchy;
import com.strandls.taxonomy.pojo.request.TaxonomySave;
import com.strandls.taxonomy.service.CommonNameSerivce;
import com.strandls.taxonomy.service.RankSerivce;
import com.strandls.taxonomy.service.TaxonomyDefinitionSerivce;
import com.strandls.taxonomy.util.AbstractService;
import com.strandls.taxonomy.util.TaxonomyUtil;
import com.strandls.utility.ApiException;
import com.strandls.utility.controller.UtilityServiceApi;
import com.strandls.utility.pojo.ParsedName;

/**
 * 
 * @author Vilay
 *
 */
public class TaxonomyDefinitionServiceImpl extends AbstractService<TaxonomyDefinition>
		implements TaxonomyDefinitionSerivce {

	@Inject
	private TaxonomyDefinitionDao taxonomyDao;

	@Inject
	private TaxonomyESUpdate taxonomyESUpdate;

	@Inject
	private TaxonomyRegistryDao taxonomyRegistryDao;

	@Inject
	private UtilityServiceApi utilityServiceApi;

	@Inject
	private RankSerivce rankService;

	@Inject
	private AcceptedSynonymDao acceptedSynonymDao;

	@Inject
	private CommonNameSerivce commonNameSerivce;

	@Inject
	private ObjectMapper objectMapper;

	static final Long UPLOADER_ID = 1L;

	@Inject
	public TaxonomyDefinitionServiceImpl(TaxonomyDefinitionDao dao) {
		super(dao);
	}

	@Override
	public TaxonomyDefinition fetchById(Long id) {
		return taxonomyDao.findById(id);
	}

	@Override
	public List<TaxonomyDefinition> saveList(HttpServletRequest request, List<TaxonomySave> taxonomyList)
			throws ApiException {
		List<TaxonomyDefinition> taxonomyDefinitions = new ArrayList<TaxonomyDefinition>();
		for (TaxonomySave taxonomySave : taxonomyList) {
			TaxonomyDefinition taxonomyDefinition = save(request, taxonomySave);
			taxonomyDefinitions.add(taxonomyDefinition);
		}
		return taxonomyDefinitions;
	}

	@Override
	public TaxonomyDefinition save(HttpServletRequest request, TaxonomySave taxonomySave) throws ApiException {

		TaxonomyDefinition taxonomyDefinition = addTaxonomyDefintionNodes(request, taxonomySave);

		if (taxonomyDefinition != null) {
			Long taxonomyId = taxonomyDefinition.getId();
			List<Long> taxonIds = new ArrayList<Long>();
			taxonIds.add(taxonomyId);
			taxonomyESUpdate.pushUpdateToElastic(taxonIds);
		}

		return taxonomyDefinition;
	}

	private TaxonomyDefinition addTaxonomyDefintionNodes(HttpServletRequest request, TaxonomySave taxonomySave)
			throws ApiException {
		Map<String, TaxonomyDefinition> createdTaxonomy = addNodes(request, taxonomySave);

		if (createdTaxonomy == null || createdTaxonomy.isEmpty())
			return null;

		return createdTaxonomy.get(taxonomySave.getRank().toLowerCase());
	}

	private Map<String, TaxonomyDefinition> addNodes(HttpServletRequest request, TaxonomySave taxonomySave)
			throws ApiException {

		Map<String, TaxonomyDefinition> createdTaxonomy = new LinkedHashMap<String, TaxonomyDefinition>();

		Map<String, String> constructedRanks = constructRanksForTheInput(request, taxonomySave, taxonomySave.getRank(),
				taxonomySave.getScientificName());
		TaxonomyCreationHierarchy taxonomyCreationHierarchy = getTaxonomyCreationHierarchy(constructedRanks);
		Long matchedTaxonId = taxonomyCreationHierarchy.getMatchedTaxonId();
		TaxonomyRegistry registry = taxonomyRegistryDao.findbyTaxonomyId(matchedTaxonId);
		String parentPath = registry.getPath();
		StringBuilder path = new StringBuilder(parentPath);

		for (Map.Entry<String, ParsedName> entry : taxonomyCreationHierarchy.getUnmatchedNodeToCreate().entrySet()) {
			String nodeRank = entry.getKey();
			ParsedName parsedName = entry.getValue();
			TaxonomyDefinition taxonomyDefinition = createTaxonomyDefiniiton(parsedName, nodeRank,
					taxonomySave.getStatus(), taxonomySave.getPosition(), taxonomySave.getSource(),
					taxonomySave.getSourceId());
			createdTaxonomy.put(nodeRank, taxonomyDefinition);
			Long taxonomyDefinitionId = taxonomyDefinition.getId();
			path.append(".");
			path.append(taxonomyDefinitionId);
			registry = new TaxonomyRegistry();
			registry.setClassificationId(1L);
			registry.setPath(path.toString());
			registry.setTaxonomyDefinationId(taxonomyDefinitionId);
			registry.setRank(nodeRank);
			taxonomyRegistryDao.save(registry);
		}
		// Add the synonyms if present for the given accepted names in definition and
		// accepted-synonyms
		TaxonomyDefinition acceptedTaxonomy;
		if (taxonomyCreationHierarchy.getUnmatchedNodeToCreate().isEmpty()
				&& taxonomyCreationHierarchy.getMatchedRank().equalsIgnoreCase(taxonomySave.getRank())) {
			acceptedTaxonomy = taxonomyDao.findById(matchedTaxonId);
		} else {
			acceptedTaxonomy = createdTaxonomy.get(taxonomySave.getRank().toLowerCase());
		}
		String synonyms = taxonomySave.getSynonyms();
		if (synonyms != null && !"".equals(synonyms.trim())
				&& (acceptedTaxonomy != null && TaxonomyStatus.ACCEPTED.name().equals(acceptedTaxonomy.getStatus()))) {
			String synonymRank = acceptedTaxonomy.getRank();
			Long acceptedId = acceptedTaxonomy.getId();
			for (String synonym : synonyms.split(";")) {
				ParsedName parsedName = utilityServiceApi.getNameParsed(synonym);
				TaxonomyDefinition synonymTaxonomy = findByCanonicalName(parsedName, synonymRank,
						TaxonomyStatus.SYNONYM.name());
				if (synonymTaxonomy == null
						|| TaxonomyStatus.ACCEPTED.name().equalsIgnoreCase(synonymTaxonomy.getStatus())) {
					synonymTaxonomy = createTaxonomyDefiniiton(parsedName, synonymRank, TaxonomyStatus.SYNONYM,
							taxonomySave.getPosition(), taxonomySave.getSource(), taxonomySave.getSourceId());
				}
				Long synonymId = synonymTaxonomy.getId();
				AcceptedSynonym acceptedSynonym = acceptedSynonymDao.findByAccpetedIdSynonymId(acceptedId, synonymId);
				if (acceptedSynonym == null) {
					acceptedSynonym = new AcceptedSynonym();
					acceptedSynonym.setAcceptedId(acceptedId);
					acceptedSynonym.setSynonymId(synonymId);
					acceptedSynonym.setVersion(0L);
					acceptedSynonymDao.save(acceptedSynonym);
				}
			}
		}

		// Add the common names into the system
		Map<Long, String[]> languageIdToCommonNames = taxonomySave.getCommonNames();
		String source = acceptedTaxonomy.getViaDatasource();
		if (acceptedTaxonomy != null && languageIdToCommonNames != null && !languageIdToCommonNames.isEmpty()) {
			Long taxonConceptId = acceptedTaxonomy.getId();
			commonNameSerivce.addCommonNames(taxonConceptId, languageIdToCommonNames, source);
		}
		return createdTaxonomy;
	}

	private TaxonomyCreationHierarchy getTaxonomyCreationHierarchy(Map<String, String> constructedRanks)
			throws ApiException {
		LinkedList<String> unmatchedRanks = new LinkedList<String>();
		TaxonomyDefinition taxonomyDefinition = null;
		Map<String, ParsedName> rankToParsedName = new HashMap<String, ParsedName>();
		for (Map.Entry<String, String> entry : constructedRanks.entrySet()) {
			String rankName = entry.getKey();
			String scientificName = entry.getValue();
			ParsedName parsedName = utilityServiceApi.getNameParsed(scientificName);
			rankToParsedName.put(rankName, parsedName);
			taxonomyDefinition = findByCanonicalName(parsedName, rankName, TaxonomyStatus.ACCEPTED.name());
			if (taxonomyDefinition != null)
				break;
			unmatchedRanks.addFirst(rankName);
		}
		TaxonomyCreationHierarchy taxonomyCreationHierarchy = new TaxonomyCreationHierarchy();
		if (taxonomyDefinition == null) { // First entry without the ROOT element in definition and registry
			ParsedName parsedName = utilityServiceApi.getNameParsed("Root");
			taxonomyDefinition = createTaxonomyDefiniiton(parsedName, "root", TaxonomyStatus.ACCEPTED,
					TaxonomyPosition.CLEAN, null, null);
			Long taxonomyDefinitionId = taxonomyDefinition.getId();
			String path = taxonomyDefinitionId.toString();

			TaxonomyRegistry registry = new TaxonomyRegistry();
			registry.setClassificationId(1L);
			registry.setPath(path);
			registry.setTaxonomyDefinationId(taxonomyDefinitionId);
			registry.setRank("root");
			taxonomyRegistryDao.save(registry);
			unmatchedRanks.removeFirst();
		}
		taxonomyCreationHierarchy.setMatchedRank(taxonomyDefinition.getRank());
		taxonomyCreationHierarchy.setMatchedTaxonId(taxonomyDefinition.getId());
		for (String unmatchedRank : unmatchedRanks) {
			taxonomyCreationHierarchy.addUnmatchedNodeToCreate(unmatchedRank, rankToParsedName.get(unmatchedRank));
		}
		return taxonomyCreationHierarchy;
	}

	private TaxonomyDefinition createTaxonomyDefiniiton(ParsedName parsedName, String rankName,
			TaxonomyStatus taxonomyStatus, TaxonomyPosition taxonomyPosition, String source, String sourceId) {
		String canonicalName = parsedName.getCanonicalName().getFull();
		String[] nameTokens = canonicalName.split(" ");
		String binomialName;
		if (nameTokens.length >= 2)
			binomialName = nameTokens[0] + nameTokens[1];
		else
			binomialName = canonicalName;
		Timestamp uploadTime = new Timestamp(new Date().getTime());
		Long uploaderId = UPLOADER_ID;
		String status = taxonomyStatus.name();
		String position = taxonomyPosition.name();
		String classs = "species.TaxonomyDefinition";

		TaxonomyDefinition taxonomyDefinition = new TaxonomyDefinition();
		taxonomyDefinition.setBinomialForm(binomialName);
		taxonomyDefinition.setCanonicalForm(canonicalName);
		taxonomyDefinition.setName(parsedName.getVerbatim());
		taxonomyDefinition.setNormalizedForm(parsedName.getNormalized());
		taxonomyDefinition.setRank(rankName);
		taxonomyDefinition.setUploadTime(uploadTime);
		taxonomyDefinition.setUploaderId(uploaderId);
		taxonomyDefinition.setStatus(status);
		taxonomyDefinition.setPosition(position);
		taxonomyDefinition.setClasss(classs);
		taxonomyDefinition.setViaDatasource(source);
		taxonomyDefinition.setNameSourceId(sourceId);
		taxonomyDefinition.setAuthorYear(parsedName.getAuthorship());
		taxonomyDefinition.setIsDeleted(false);
		taxonomyDefinition = save(taxonomyDefinition);
		return taxonomyDefinition;
	}

	private Map<String, String> constructRanksForTheInput(HttpServletRequest request, TaxonomySave taxonomySave,
			String rankName, String scientificName) {
		List<Rank> ranks = rankService.getAllRank(request);
		Map<String, String> inputRanks = taxonomySave.getRankToName();
		Double highestInputRank = TaxonomyUtil.getHighestInputRank(ranks, inputRanks);
		Map<String, String> constructedRanks = new LinkedHashMap<String, String>();
		constructedRanks.put(rankName.toLowerCase(), scientificName);
		for (Rank rank : ranks) {
			if (rank.getRankValue() > highestInputRank)
				continue;

			if (rank.getRankValue() == 0.0)
				constructedRanks.put(rank.getName(), "Root");
			else if (rank.getIsRequired().booleanValue() && !inputRanks.containsKey(rank.getName()))
				throw new IllegalArgumentException("Input should contain all the required ranks");
			else if (inputRanks.containsKey(rank.getName()) && !"".equals(inputRanks.get(rank.getName())))
				constructedRanks.put(rank.getName().toLowerCase(), inputRanks.get(rank.getName()));
		}
		return constructedRanks;
	}

	private TaxonomyDefinition findByCanonicalName(ParsedName parsedName, String rankName, String status) {

		String canonicalName = parsedName.getCanonicalName().getFull();
		List<TaxonomyDefinition> taxonomyDefinitions = taxonomyDao.findByCanonicalForm(canonicalName, rankName);

		if (taxonomyDefinitions.isEmpty())
			return null;

		String verbatim = parsedName.getVerbatim();
		for (TaxonomyDefinition taxonomyDefinition : taxonomyDefinitions) {
			if (!status.equalsIgnoreCase(taxonomyDefinition.getStatus()))
				continue;
			if (taxonomyDefinition.getName().equals(verbatim)) {
				// TODO : check for the first three level if same return the taxonomy
				return taxonomyDefinition;
			}
		}

		return null;
	}

	@Override
	public Map<String, Object> uploadFile(HttpServletRequest request, FormDataMultiPart multiPart)
			throws IOException, ApiException, InterruptedException, ExecutionException {
		Long startTime = System.currentTimeMillis();
		FormDataBodyPart formdata = multiPart.getField("metadata");
		if (formdata == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
		}
		InputStream metaDataInputStream = formdata.getValueAs(InputStream.class);
		InputStreamReader inputStreamReader = new InputStreamReader(metaDataInputStream, StandardCharsets.UTF_8);
		FileMetadata fileMetaData = objectMapper.readValue(inputStreamReader, FileMetadata.class);

		if ("csv".equalsIgnoreCase(fileMetaData.getFileType())) {
			formdata = multiPart.getField("csv");
			InputStream csvDataInputStream = formdata.getValueAs(InputStream.class);
			inputStreamReader = new InputStreamReader(csvDataInputStream, StandardCharsets.UTF_8);
			CSVReader reader = new CSVReader(inputStreamReader);
			Iterator<String[]> it = reader.iterator();
			String[] headers = it.next();
			fileMetaData.updateIndices(headers);

			List<Long> taxonIds = new ArrayList<Long>();
			while (it.hasNext()) {
				String[] data = it.next();
				TaxonomySave taxonomySave = fileMetaData.readOneRow(utilityServiceApi, data);
				if (taxonomySave != null) {
					TaxonomyDefinition taxonomyDefinition = addTaxonomyDefintionNodes(request, taxonomySave);
					if (taxonomyDefinition != null)
						taxonIds.add(taxonomyDefinition.getId());
					else {
						// These taxonomy are not inserted. 
						// 1. May be already exists
						// 2. throws exception here
					}
				}
			}
			if(!taxonIds.isEmpty()) {
				taxonomyESUpdate.pushUpdateToElastic(taxonIds);
			}
			reader.close();
		}
		Long endTime = System.currentTimeMillis();
		System.out.println("Total time : " + (endTime - startTime));
		return null;
	}
}
