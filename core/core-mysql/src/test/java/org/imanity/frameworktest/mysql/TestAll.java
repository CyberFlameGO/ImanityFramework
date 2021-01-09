package org.imanity.frameworktest.mysql;

import org.imanity.framework.mysql.connection.file.H2ConnectionFactory;
import org.imanity.framework.mysql.pojo.Transaction;
import org.junit.Test;

import javax.persistence.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestAll {

	@Test
	public void debug() {

		H2ConnectionFactory factory = new H2ConnectionFactory(new File("./h2test").toPath().toAbsolutePath(), true);
		
		// db.setSqlMaker(new PostgresMaker()); // set this to match your sql flavor		
		
		/* test straight sql */
		factory.query().sql("drop table if exists names").execute();
		
		/* test create table */
		factory.query().createTable(Name.class);
		
		/* test inserts */
		Name john = new Name("John", "Doe");
		factory.query().insert(john);
		
		Name bill = new Name("Bill", "Smith");
		factory.query().insert(bill);
		
		/* test where clause, also id and generated values */
		List<Name> list = factory.query().whereQuery("firstName", "John").results(Name.class);
		dump("john only:", list);
		
		/* test delete single record */
		factory.query().delete(john);
		List<Name> list1 = factory.query().orderBy("lastName").results(Name.class);
		dump("bill only:", list1);
		
		/* test update single record */
		bill.firstName = "Joe";
		int rowsAffected = factory.query().update(bill).getRowsAffected();
		List<Name> list2 = factory.query().results(Name.class);
		dump("bill is now joe, and rowsAffected=" + rowsAffected, list2);
		
		/* test using a map for results instead of a pojo */
		Map<?, ?> map = factory.query().sql("select count(*) as count from names").first(HashMap.class);
		System.out.println("Num records (should be 1):" + map.get("count"));
		
		/* test using a primitive for results instead of a pojo */
		Long count = factory.query().sql("select count(*) as count from names").first(Long.class);
		System.out.println("Num records (should be 1):" + count);
		
		/* test delete with where clause */
		factory.query().table("names").whereQuery("firstName", "Joe").delete();

		/* make sure the delete happened */
		count = factory.query().sql("select count(*) as count from names").first(Long.class);
		System.out.println("Num records (should be 0):" + count);
		
		/* test transactions */
		factory.query().insert(new Name("Fred", "Jones"));
		Transaction trans = factory.startTransaction();
		factory.query().transaction(trans).insert(new Name("Sam", "Williams"));
		factory.query().transaction(trans).insert(new Name("George ", "Johnson"));
		trans.rollback();
		List<Name> list3 = factory.query().results(Name.class);
		dump("fred only:", list3);
		
		//db.sql("drop table names").execute();
	}

	
	
	public static void dump(String label, List<Name> list) {
		System.out.println(label);
		for (Name n: list) {
			System.out.println(n.toString());
		}
	}


	@Table(name="names")
	static public class Name {
		
		// must have 0-arg constructor
		public Name() {} 
		
		// can also have convenience constructor
		public Name(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		// primary key, generated on the server side
		@Id
		@GeneratedValue 
		public long id;
		
		// a public property without getter or setter
		@Column(name="firstname")  // must do this for Postgres
		public String firstName; 
		
		// a private property with getter and setter below
		@Column(name="lastname")
		private String lastName;

		@Transient
		public String ignoreMe;
		
		// ignore static fields
		public static String ignoreThisToo;
		
		public String toString() {
			return id + " " + firstName + " " + lastName;
		}
	}
	
}
