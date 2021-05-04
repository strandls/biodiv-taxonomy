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
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.dao.AcceptedSynonymDao;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.dao.TaxonomyRegistryDao;
import com.strandls.taxonomy.pojo.AcceptedSynonym;
import com.strandls.taxonomy.pojo.CommonName;
import com.strandls.taxonomy.pojo.Rank;
import com.strandls.taxonomy.pojo.SynonymData;
import com.strandls.taxonomy.pojo.TaxonomicNames;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.pojo.enumtype.TaxonomyPosition;
import com.strandls.taxonomy.pojo.enumtype.TaxonomyStatus;
import com.strandls.taxonomy.pojo.request.FileMetadata;
import com.strandls.taxonomy.pojo.request.TaxonomyCreationHierarchy;
import com.strandls.taxonomy.pojo.request.TaxonomySave;
import com.strandls.taxonomy.pojo.response.TaxonomyDefinitionAndRegistry;
import com.strandls.taxonomy.pojo.response.TaxonomyRegistryResponse;
import com.strandls.taxonomy.pojo.response.TaxonomySearch;
import com.strandls.taxonomy.service.CommonNameSerivce;
import com.strandls.taxonomy.service.RankSerivce;
import com.strandls.taxonomy.service.TaxonomyDefinitionSerivce;
import com.strandls.taxonomy.service.exception.TaxonCreationException;
import com.strandls.taxonomy.service.exception.UnRecongnizedRankException;
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

	@Inject
	private LogActivities logActivity;

	private final Logger logger = LoggerFactory.getLogger(TaxonomyDefinitionServiceImpl.class);

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
		Map<String, TaxonomyDefinition> createdTaxonomy;
		try {
			createdTaxonomy = addNodes(request, taxonomySave);
		} catch (TaxonCreationException e) {
			return null;
		}

		if (createdTaxonomy == null || createdTaxonomy.isEmpty())
			return null;

		return createdTaxonomy.get(taxonomySave.getRank().toLowerCase());
	}

	private Map<String, TaxonomyDefinition> addNodes(HttpServletRequest request, TaxonomySave taxonomySave)
			throws ApiException, TaxonCreationException {

		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		Long userId = Long.parseLong(profile.getId());
		Map<String, TaxonomyDefinition> createdTaxonomy = new LinkedHashMap<String, TaxonomyDefinition>();

		Map<String, String> constructedRanks = constructRanksForTheInput(request, taxonomySave, taxonomySave.getRank(),
				taxonomySave.getScientificName());
		TaxonomyCreationHierarchy taxonomyCreationHierarchy = getTaxonomyCreationHierarchy(constructedRanks,
				taxonomySave);
		Long matchedTaxonId = taxonomyCreationHierarchy.getMatchedTaxonId();
		TaxonomyRegistry registry = taxonomyRegistryDao.findbyTaxonomyId(matchedTaxonId);
		String parentPath = registry.getPath();
		StringBuilder path = new StringBuilder(parentPath);

		for (Map.Entry<String, ParsedName> entry : taxonomyCreationHierarchy.getUnmatchedNodeToCreate().entrySet()) {
			String nodeRank = entry.getKey();
			ParsedName parsedName = entry.getValue();
			TaxonomyDefinition taxonomyDefinition = createTaxonomyDefiniiton(parsedName, nodeRank,
					taxonomySave.getStatus(), taxonomySave.getPosition(), taxonomySave.getSource(),
					taxonomySave.getSourceId(), userId);
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
			Long acceptedId = acceptedTaxonomy.getId();
			String acceptedRank = acceptedTaxonomy.getRank();
			for (String synonym : synonyms.split(";")) {
				ParsedName parsedName = utilityServiceApi.getNameParsed(synonym);
				String synonymRank;
				try {
					synonymRank = TaxonomyUtil.getRankForSynonym(parsedName, acceptedRank);
				} catch (UnRecongnizedRankException e) {
					// Dropping this synonym
					continue;
				}
				TaxonomyDefinition synonymTaxonomy = findByCanonicalName(parsedName, synonymRank,
						TaxonomyStatus.SYNONYM, taxonomySave);
				if (synonymTaxonomy == null
						|| TaxonomyStatus.ACCEPTED.name().equalsIgnoreCase(synonymTaxonomy.getStatus())) {
					synonymTaxonomy = createTaxonomyDefiniiton(parsedName, synonymRank, TaxonomyStatus.SYNONYM,
							taxonomySave.getPosition(), taxonomySave.getSource(), taxonomySave.getSourceId(), userId);
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

	private TaxonomyCreationHierarchy getTaxonomyCreationHierarchy(Map<String, String> constructedRanks,
			TaxonomySave taxonomySave) throws ApiException, TaxonCreationException {
		LinkedList<String> unmatchedRanks = new LinkedList<String>();
		TaxonomyDefinition taxonomyDefinition = null;
		Map<String, ParsedName> rankToParsedName = new HashMap<String, ParsedName>();
		for (Map.Entry<String, String> entry : constructedRanks.entrySet()) {
			String rankName = entry.getKey();
			String scientificName = entry.getValue();
			ParsedName parsedName = utilityServiceApi.getNameParsed(scientificName);
			rankToParsedName.put(rankName, parsedName);
			taxonomyDefinition = findByCanonicalName(parsedName, rankName, TaxonomyStatus.ACCEPTED, taxonomySave);
			if (taxonomyDefinition != null)
				break;
			unmatchedRanks.addFirst(rankName);
		}
		TaxonomyCreationHierarchy taxonomyCreationHierarchy = new TaxonomyCreationHierarchy();
		if (taxonomyDefinition == null) { // First entry without the ROOT element in definition and registry
			ParsedName parsedName = utilityServiceApi.getNameParsed("Root");
			taxonomyDefinition = createTaxonomyDefiniiton(parsedName, "root", TaxonomyStatus.ACCEPTED,
					TaxonomyPosition.CLEAN, null, null, null);
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

	private TaxonomyDefinition updateTaxonomyDefinition(Long taxonId, ParsedName parsedName, String rankName,
			TaxonomyStatus taxonomyStatus, TaxonomyPosition taxonomyPosition, String source, String sourceId,
			Long uploaderId) {

		TaxonomyDefinition taxonomyDefinition = taxonomyDao.findById(taxonId);

		String canonicalName = parsedName.getCanonicalName().getFull();
		String[] nameTokens = canonicalName.split(" ");
		String binomialName;
		if (nameTokens.length >= 2)
			binomialName = nameTokens[0] + " " + nameTokens[1];
		else
			binomialName = canonicalName;
		String italicisedForm = TaxonomyUtil.getItalicisedForm(parsedName, rankName);
		String status = taxonomyStatus.name();
		String position = taxonomyPosition.name();

		taxonomyDefinition.setBinomialForm(binomialName);
		taxonomyDefinition.setCanonicalForm(canonicalName);
		taxonomyDefinition.setItalicisedForm(italicisedForm);
		taxonomyDefinition.setName(parsedName.getVerbatim().trim());
		taxonomyDefinition.setNormalizedForm(parsedName.getNormalized());
		taxonomyDefinition.setRank(rankName);
		taxonomyDefinition.setStatus(status);
		taxonomyDefinition.setPosition(position);
		taxonomyDefinition.setUploaderId(uploaderId);
		taxonomyDefinition.setViaDatasource(source);
		taxonomyDefinition.setNameSourceId(sourceId);
		taxonomyDefinition.setAuthorYear(parsedName.getAuthorship());

		taxonomyDefinition = taxonomyDao.update(taxonomyDefinition);
		return taxonomyDefinition;

	}

	private TaxonomyDefinition createTaxonomyDefiniiton(ParsedName parsedName, String rankName,
			TaxonomyStatus taxonomyStatus, TaxonomyPosition taxonomyPosition, String source, String sourceId,
			Long userId) throws TaxonCreationException {
		if (parsedName == null || parsedName.getCanonicalName() == null)
			throw new TaxonCreationException("Not valid name");
		String canonicalName = parsedName.getCanonicalName().getFull();
		String binomialName = TaxonomyUtil.getBinomialName(canonicalName);
		String italicisedForm = TaxonomyUtil.getItalicisedForm(parsedName, rankName);
		Timestamp uploadTime = new Timestamp(new Date().getTime());
		Long uploaderId = null;
		if (userId == null)
			uploaderId = UPLOADER_ID;
		else
			uploaderId = userId;
		String status = taxonomyStatus.name();
		String position = taxonomyPosition.name();
		String classs = "species.TaxonomyDefinition";

		TaxonomyDefinition taxonomyDefinition = new TaxonomyDefinition();
		taxonomyDefinition.setBinomialForm(binomialName);
		taxonomyDefinition.setCanonicalForm(canonicalName);
		taxonomyDefinition.setItalicisedForm(italicisedForm);
		taxonomyDefinition.setName(parsedName.getVerbatim().trim());
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

	private TaxonomyDefinition findByCanonicalName(ParsedName parsedName, String rankName, TaxonomyStatus status,
			TaxonomySave taxonomySave) throws ApiException {

		if (parsedName == null || parsedName.getCanonicalName() == null)
			return null;

		String canonicalName = parsedName.getCanonicalName().getFull();
		List<TaxonomyDefinition> taxonomyDefinitions = taxonomyDao.findByCanonicalForm(canonicalName, rankName);

		if (taxonomyDefinitions.isEmpty())
			return null;

		boolean isLeaf = TaxonomyStatus.SYNONYM.equals(status) || taxonomySave.getRank().equalsIgnoreCase(rankName);

		String verbatim = parsedName.getVerbatim();
		if (isLeaf) {
			for (TaxonomyDefinition taxonomyDefinition : taxonomyDefinitions) {
				if (!status.name().equalsIgnoreCase(taxonomyDefinition.getStatus()))
					continue;
				if (taxonomyDefinition.getName().equals(verbatim))
					return taxonomyDefinition;
			}
		} else {
			Map<String, String> rankToName = taxonomySave.getRankToName();
			rankToName.put("root", "Root");
			Map<String, ParsedName> rankToParsedName = new HashMap<String, ParsedName>();
			for (Map.Entry<String, String> entry : rankToName.entrySet()) {
				String scientificName = entry.getValue();
				ParsedName pName = utilityServiceApi.getNameParsed(scientificName);
				rankToParsedName.put(entry.getKey(), pName);
			}
			int maxScore = 0;
			TaxonomyDefinition matchedTaxonomy = null;
			for (TaxonomyDefinition taxonomyDefinition : taxonomyDefinitions) {
				List<Object[]> hierarchy = taxonomyRegistryDao.getHierarchy(taxonomyDefinition.getId());
				int score = 0;
				for (Object[] row : hierarchy) {
					String rank = (String) row[1];
					ParsedName inputParsedName = rankToParsedName.get(rank);
					if (inputParsedName.getCanonicalName().getFull().equalsIgnoreCase((String) row[2]))
						score++;
				}
				if (maxScore < score) {
					maxScore = score;
					matchedTaxonomy = taxonomyDefinition;
				}
			}
			return matchedTaxonomy;
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
			if (!taxonIds.isEmpty()) {
				taxonomyESUpdate.pushUpdateToElastic(taxonIds);
			}
			reader.close();
		}
		Long endTime = System.currentTimeMillis();
		System.out.println("Total time : " + (endTime - startTime));
		return null;
	}

	@Override
	public TaxonomicNames findSynonymCommonName(Long taxonId) {

		try {
			List<CommonName> commonNames = commonNameSerivce.fetchCommonNameWithLangByTaxonId(taxonId);
			List<TaxonomyDefinition> synonymList = findSynonyms(taxonId);

			TaxonomicNames result = new TaxonomicNames(commonNames, synonymList);
			return result;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;

	}

	private List<TaxonomyDefinition> findSynonyms(Long taxonId) {
		List<AcceptedSynonym> acceptedSynonymsList = acceptedSynonymDao.findByAccepetdId(taxonId);
		List<TaxonomyDefinition> synonymList = new ArrayList<TaxonomyDefinition>();
		if (acceptedSynonymsList != null && !acceptedSynonymsList.isEmpty()) {
			for (AcceptedSynonym synonym : acceptedSynonymsList) {
				TaxonomyDefinition taxonomy = taxonomyDao.findById(synonym.getSynonymId());
				synonymList.add(taxonomy);
			}
		}
		return synonymList;
	}

	@Override
	public List<TaxonomyDefinition> updateAddSynonym(HttpServletRequest request, Long speciesId, Long taxonId,
			SynonymData synonymData) {

		try {
			ParsedName parsedName = utilityServiceApi.getNameParsed(synonymData.getName());
			String synonymRank = TaxonomyUtil.getRankForSynonym(parsedName, synonymData.getRank());
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);

			Long userId = Long.parseLong(profile.getId());

			TaxonomyDefinition synonymTaxonomy = null;
			String desc = "";
			String activityType = "";
			Long synonymId = null;
			String name = "";
			TaxonomyDefinition synonymCheck = findByCanonicalName(parsedName, synonymRank, TaxonomyStatus.SYNONYM,
					null);

			if (synonymData.getId() == null) {
				if (synonymCheck == null) {

					synonymTaxonomy = createTaxonomyDefiniiton(parsedName, synonymRank, TaxonomyStatus.SYNONYM,
							TaxonomyPosition.RAW, synonymData.getDataSource(), synonymData.getDataSourceId(), userId);
					synonymId = synonymTaxonomy.getId();
					name = synonymTaxonomy.getName();
				} else {
					synonymId = synonymCheck.getId();
					name = synonymCheck.getName();
				}

				AcceptedSynonym acceptedSynonym = acceptedSynonymDao.findByAccpetedIdSynonymId(taxonId, synonymId);
				if (acceptedSynonym == null) {
					acceptedSynonym = new AcceptedSynonym();
					acceptedSynonym.setAcceptedId(taxonId);
					acceptedSynonym.setSynonymId(synonymId);
					acceptedSynonym.setVersion(0L);
					acceptedSynonymDao.save(acceptedSynonym);
				}

				desc = "Added synonym : " + name;

				activityType = "Added synonym";

			} else {

				if (synonymCheck == null) {
					synonymTaxonomy = updateTaxonomyDefinition(synonymData.getId(), parsedName, synonymRank,
							TaxonomyStatus.SYNONYM, TaxonomyPosition.RAW, synonymData.getDataSource(),
							synonymData.getDataSourceId(), userId);
					synonymId = synonymTaxonomy.getId();
					name = synonymTaxonomy.getName();
				} else {
//					delete previous synonym
					synonymId = synonymCheck.getId();
					name = synonymCheck.getName();
					AcceptedSynonym acceptedSynonym = acceptedSynonymDao.findByAccpetedIdSynonymId(taxonId,
							synonymData.getId());
					acceptedSynonymDao.delete(acceptedSynonym);
					TaxonomyDefinition taxnomyDefinition = taxonomyDao.findById(synonymData.getId());
					taxnomyDefinition.setIsDeleted(true);
					taxonomyDao.update(taxnomyDefinition);

//					map the exist synonym
					acceptedSynonym = new AcceptedSynonym();
					acceptedSynonym.setAcceptedId(taxonId);
					acceptedSynonym.setSynonymId(synonymId);
					acceptedSynonym.setVersion(0L);
					acceptedSynonymDao.save(acceptedSynonym);

				}

				desc = "Updated synonym : " + name;
				activityType = "Updated synonym";
			}

			logActivity.LogActivity(request.getHeader(HttpHeaders.AUTHORIZATION), desc, speciesId, speciesId, "species",
					synonymId, activityType, null);

			return findSynonyms(taxonId);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<TaxonomyDefinition> deleteSynonym(HttpServletRequest request, Long speciesId, Long taxonId,
			Long synonymId) {
		try {
			AcceptedSynonym acceptedSynonym = acceptedSynonymDao.findByAccpetedIdSynonymId(taxonId, synonymId);
			acceptedSynonymDao.delete(acceptedSynonym);
			TaxonomyDefinition taxnomyDefinition = taxonomyDao.findById(synonymId);
			taxnomyDefinition.setIsDeleted(true);
			taxonomyDao.update(taxnomyDefinition);

			String desc = "Deleted synonym : " + taxnomyDefinition.getName();

			logActivity.LogActivity(request.getHeader(HttpHeaders.AUTHORIZATION), desc, speciesId, speciesId, "species",
					taxnomyDefinition.getId(), "Deleted synonym", null);
			return findSynonyms(taxonId);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TaxonomySearch getByNameSearch(String scientificName, String rankName) throws ApiException {
		TaxonomySearch taxonomySearch = new TaxonomySearch();
		ParsedName parsedName = utilityServiceApi.getNameParsed(scientificName);
		String canonicalForm = parsedName.getCanonicalName().getFull();
		List<TaxonomyDefinition> taxonomyDefinitions = taxonomyDao.findByCanonicalForm(canonicalForm, rankName);
		if (taxonomyDefinitions.isEmpty()) {
			List<Object> details = parsedName.getDetails();
			if (details.get(0) instanceof LinkedHashMap) {
				Map<String, Object> m = (Map<String, Object>) details.get(0);
				if (m.containsKey(TaxonomyUtil.GENUS))
					canonicalForm = (String) ((LinkedHashMap<String, Object>) m.get(TaxonomyUtil.GENUS)).get("value");
				else if (m.containsKey(TaxonomyUtil.UNINOMIAL)) {
					canonicalForm = (String) ((LinkedHashMap<String, Object>) m.get(TaxonomyUtil.UNINOMIAL))
							.get("value");
				}
				taxonomyDefinitions = taxonomyDao.findByCanonicalForm(canonicalForm, TaxonomyUtil.GENUS);

				if (!taxonomyDefinitions.isEmpty()) {
					// Taking the first one to auto Fill
					List<TaxonomyDefinitionAndRegistry> parentMatched = new ArrayList<TaxonomyDefinitionAndRegistry>();
					//Map<Long, List<TaxonomyRegistryResponse>> parentMatched = new HashMap<Long, List<TaxonomyRegistryResponse>>();
					for (TaxonomyDefinition taxonomyDefinition : taxonomyDefinitions) {
						List<TaxonomyRegistryResponse> taxonomyRegistry = taxonomyRegistryDao
								.getPathToRoot(taxonomyDefinition.getId());
						TaxonomyDefinitionAndRegistry taxonomyDefinitionAndRegistry = new TaxonomyDefinitionAndRegistry(taxonomyDefinition, taxonomyRegistry);
						parentMatched.add(taxonomyDefinitionAndRegistry);
					}
					taxonomySearch.setParentMatched(parentMatched);
				}
			}
		} else {
			List<TaxonomyDefinitionAndRegistry> matched = new ArrayList<TaxonomyDefinitionAndRegistry>();
			for(TaxonomyDefinition taxonomyDefinition : taxonomyDefinitions) {
				List<TaxonomyRegistryResponse> taxonomyRegistry = taxonomyRegistryDao
						.getPathToRoot(taxonomyDefinition.getId());
				TaxonomyDefinitionAndRegistry taxonomyDefinitionAndRegistry = new TaxonomyDefinitionAndRegistry(taxonomyDefinition, taxonomyRegistry);
				matched.add(taxonomyDefinitionAndRegistry);
			}
			taxonomySearch.setMatched(matched);
		}

		return taxonomySearch;
	}
	
	@Override
	public TaxonomyDefinition updateName(Long taxonId, String taxonName) throws ApiException {
		TaxonomyDefinition taxonomyDefinition;
		try {
			taxonomyDefinition = findById(taxonId);
		} catch (NoResultException e) {
			throw new NoResultException("Not able to find the given taxon");
		}
		
		ParsedName parsedName = utilityServiceApi.getNameParsed(taxonName);
		
		String name = parsedName.getVerbatim().trim();
		String normalizedName = parsedName.getNormalized();
		String canonicalName = parsedName.getCanonicalName().getFull();
		String binomialForm = TaxonomyUtil.getBinomialName(canonicalName);
		String italicisedForm = TaxonomyUtil.getItalicisedForm(parsedName, taxonomyDefinition.getRank());
		String authorShip = parsedName.getAuthorship();
		
		taxonomyDefinition.setName(name);
		taxonomyDefinition.setNormalizedForm(normalizedName);
		taxonomyDefinition.setCanonicalForm(canonicalName);
		taxonomyDefinition.setBinomialForm(binomialForm);
		taxonomyDefinition.setItalicisedForm(italicisedForm);
		taxonomyDefinition.setAuthorYear(authorShip);
		
		Map<String, Object> esPropertiesToUpdate = new HashMap<String, Object>();
		esPropertiesToUpdate.put("name", name);
		esPropertiesToUpdate.put("canonical_form", canonicalName);
		esPropertiesToUpdate.put("italicised_form", italicisedForm);
		
		taxonomyESUpdate.pushDocumentUpdateToElastic(taxonId, esPropertiesToUpdate);
		
		
		
		return taxonomyDefinition;
	}
}
