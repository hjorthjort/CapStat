package capstat.model.statistics;

import capstat.infrastructure.database.MatchResultBlueprint;
import capstat.infrastructure.database.PartialSequenceBlueprint;
import capstat.model.user.*;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jakob
 * Created by Jakob on 31/05/15.
 */
public class MatchCountTest {
    UserLedger ul = UserLedger.getInstance();
    @Test
    public void testGetStatistics() throws Exception {
        String nickname = "DummyOne";
        String name = "Dummy Player 1";
        String password = "foobar";
        String hashedPassword = Security.hashPassword(password);

        LocalDate birthday = LocalDate.of(1992, 1, 13);
        Admittance admittance = new Admittance(Year.of(2013), Admittance
                .Period.ONE);
        ChalmersAge chalmersAge = new ChalmersAge(birthday, admittance);

        ELORanking ranking = ELORanking.defaultRanking();

        ul.registerNewUser(nickname, name, password, birthday, Year.of(2013), 1);
        ul.registerNewUser("DummyTwo", name, password, birthday, Year.of(2014), 1);
        ul.registerNewUser("DummyThree", name, password, birthday, Year.of(2014), 2);
        boolean[] glassState = new boolean[] {
                true, true, true, true, true, true, true
        };
        boolean[] matchSequence = new boolean[] {
                true, false, false, true, false, false, true, false, false, true, false, false,
                true, false, false, true, false, false, true, false, false, true, false
        };
        int startingPlayer = 1;

        PartialSequenceBlueprint psbp = new PartialSequenceBlueprint(glassState, startingPlayer, false, matchSequence);
        List<PartialSequenceBlueprint> list = new LinkedList<PartialSequenceBlueprint>();
        list.add(psbp);
        MatchResultBlueprint testResult = new MatchResultBlueprint(128128093l, "DummyOne", "DummyTwo", "DummyThree", 2, 0, Instant.now().getEpochSecond(),
                Instant.now().getEpochSecond(), list);

        PartialSequenceBlueprint psbp1 = new PartialSequenceBlueprint(glassState, startingPlayer, false, matchSequence);
        List<PartialSequenceBlueprint> list1 = new LinkedList<PartialSequenceBlueprint>();
        list1.add(psbp1);
        MatchResultBlueprint testResult1 = new MatchResultBlueprint(128128094l, "DummyOne", "DummyThree", "DummyTwo", 2, 0, Instant.now().getEpochSecond(),
                Instant.now().getEpochSecond(), list1);

        MatchResult mr = new MatchResult(testResult);
        MatchResult mr1 = new MatchResult(testResult1);
        List<MatchResult> mrList = new LinkedList<MatchResult>();
        mrList.add(0, mr);
        mrList.add(1, mr1);
        MatchCount mc = new MatchCount();
        assertTrue(1.0 == mc.getStatistics(mrList, ul.getUserByNickname("DummyOne")).get(0).getValue());
        assertFalse(1.0 == mc.getStatistics(mrList, ul.getUserByNickname("DummyTwo")).get(1).getValue());
        ul.removeUserFromLedger(UserLedger.getInstance().getUserByNickname("DummyOne"));
        ul.removeUserFromLedger(UserLedger.getInstance().getUserByNickname("DummyTwo"));
    }
}