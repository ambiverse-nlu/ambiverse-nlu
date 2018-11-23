package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.genericschema;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import edu.stanford.nlp.util.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GenericDBReader implements GenericReader {

	private String tableName;

	public GenericDBReader(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public List<Fact> getFacts(String relation) throws IOException {
		List<Fact> triples = new LinkedList<>();
		Statement stmt;
		ResultSet rs;

		try {
			Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_YAGO);
			con.setAutoCommit(false);

			stmt = con.createStatement();
			stmt.setFetchSize(10000000);
			String sql = "SELECT id, subject, object " + "FROM " + tableName + " WHERE predicate='" + relation + "'";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String id = rs.getString("id");
				String subject = rs.getString("subject");
				String object = rs.getString("object");
				triples.add(new Fact(id, subject, relation, object));
			}
			rs.close();
			con.setAutoCommit(true);

			EntityLinkingManager.releaseConnection(con);
		} catch (SQLException e) {
			throw new IOException(e);
		}

		return triples;
	}

	@Override public Iterator<Fact> iterator(String relation) throws IOException {
		return getFacts(relation).iterator();
	}

	public List<Fact> getFacts(String subject, String relation, String object) throws SQLException {
		List<Fact> triples = new LinkedList<>();
		Statement stmt;
		ResultSet rs;

		Connection con = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_YAGO);

		List<String> queryCondition = new LinkedList<>();
		if (subject != null) {
			queryCondition.add("subject = '" + StringEscapeUtils.escapeSql(subject) + "'");
		}
		if (relation != null) {
			queryCondition.add("predicate = '" + StringEscapeUtils.escapeSql(relation) + "'");
		}

		if (object != null) {
			queryCondition.add("object = '" + StringEscapeUtils.escapeSql(object) + "'");
		}

		String conditionString = StringUtils.join(queryCondition, " AND ");
		stmt = con.createStatement();
		String sql = "SELECT id, subject, object, predicate  FROM " + tableName + " WHERE " + conditionString;
		rs = stmt.executeQuery(sql);

		while (rs.next()) {
			triples.add(new Fact(rs.getString("id"), rs.getString("subject"), rs.getString("predicate"),
					rs.getString("object")));
		}
		rs.close();

		EntityLinkingManager.releaseConnection(con);

		return triples;
	}

}
