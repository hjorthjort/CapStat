package capstat.tests;

import capstat.infrastructure.DatabaseConnection;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.generated.db.capstat.tables.Users;
import org.jooq.generated.db.capstat.tables.records.UsersRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jibbs on 09/05/15.
 *
 * Performs a number of tests to manipulate the database in several
 * different ways using the jooq library.
 */
public class DatabaseConnectionTest {


	private DatabaseConnection dbConn;
	private DSLContext db;

	/**
	 * Setting up the connection using the DatabaseConnection-class.
	 */
	@Before
	public void initiateConnection() {
		this.dbConn = new DatabaseConnection();
		this.db = dbConn.database;
	}

	/**
	 * Deletes all users.
	 */
	@Test
	public void deleteAllUsers() {
		db.deleteFrom(Users.USERS).execute();
	}

	/**
	 * Inserts three users.
	 */
	@Test
	public void insertUsers () {
		db.insertInto(Users.USERS, Users.USERS.NICK, Users.USERS.NAME, Users.USERS.PASS, Users.USERS.BIRTHDAY,
				Users.USERS.ADMITTANCEYEAR, Users.USERS.ADMITTANCEREADINGPERIOD, Users.USERS.ELORANK)
				.values("lol2kpe", "Johan Andersson", "lol", new java.sql.Date(2016, 02, 29), 2012, 1, "Master" ).execute();
		db.insertInto(Users.USERS, Users.USERS.NICK, Users.USERS.NAME, Users.USERS.PASS, Users.USERS.BIRTHDAY,
				Users.USERS.ADMITTANCEYEAR, Users.USERS.ADMITTANCEREADINGPERIOD, Users.USERS.ELORANK)
				.values("user1", "Arne Ranta", "lol2", new java.sql.Date(1942, 12, 30), 1971, 3, "n00b").execute();
		db.insertInto(Users.USERS, Users.USERS.NICK, Users.USERS.NAME, Users.USERS.PASS, Users.USERS.BIRTHDAY,
				Users.USERS.ADMITTANCEYEAR, Users.USERS.ADMITTANCEREADINGPERIOD, Users.USERS.ELORANK)
				.values("user2", "Ben Dover", "lol4", new java.sql.Date(1994, 06, 21), 2013, 1, "Mediocre").execute();
	}

	/**
	 * Prints the users in the database in several different ways.
	 */
	@Test
	public void getUsers() {
		Result<Record> result = db.select().from(Users.USERS).fetch();
		System.out.println("\nLISTING ALL USERS VIA RESULT : \n" + result + "\n");

		int count = 0;
		for ( UsersRecord userRecord : db.selectFrom(Users.USERS).fetch() ) {
			count++;
			System.out.print("\nPRINTING USER " + count + " VIA USERRECORD :\n" + userRecord + "\n");
		}

		System.out.println ( "\nPRINTING SECOND USERS NICK USING RESULT.GETVALUE: " + result.getValue(1,"nick") + "\n");
		System.out.println ( "\nPRINTING SECOND USERS NICK USING RESULT.GETVALUE: " + result.formatCSV() + "\n");
	}

	/**
	 * Inserts matches.
	 */
	@Test
	public void insertMatches() {
		// TODO
	}

	/**
	 * Prints the matches.
	 */
	@Test
	public void getMatches() {
		// TODO
	}

	/**
	 * Inserts throw sequences.
	 */
	@Test
	public void insertThrowSequences() {
		// TODO
	}

	/**
	 * Prints everything currently in the database.
	 */
	@Test
	public void getAll() {
		//TODO
	}

	/**
	 * Closes the connection to the database.
	 */
	@After
	public void closeConn() {
		dbConn.disconnect();
	}

}
