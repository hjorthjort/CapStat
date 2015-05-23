package capstat.infrastructure;

import java.util.Date;
import java.util.Set;

/**
 * @author jibbs
 * An interface for a Facade used to get and add matches from the database.
 */
public interface MatchDatabaseHelper {

	void addMatch(MatchResultBlueprint match);
	void addMatchSet(Set<MatchResultBlueprint> matches);
	void removeMatch(long id);
	MatchResultBlueprint getMatchById(long id);
	Set<MatchResultBlueprint> getAllMatches();
	Set<MatchResultBlueprint> getMatchesForUser(String player);
	Set<MatchResultBlueprint> getMatchesForUsers(String p1, String p2);
	Set<MatchResultBlueprint> getMatchesInDateRange(long epochFrom, long epochTo);
	Set<MatchResultBlueprint> getMatchesForSpectator(String spectator);

}
