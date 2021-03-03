package javaAssignment09;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

class Driver {
	static Connection conn = null;
	public List<Movie> populateMovies(File file) {
		List<Movie> movies = new ArrayList<Movie>();
		DateFormat format = new SimpleDateFormat("yyyy/mm/dd");
		try {
			Scanner read = new Scanner(file).useDelimiter("\n");
			while(read.hasNext()) {
				String [] movieDetails = read.next().split(",");
				
				
				Integer movieId = Integer.parseInt(movieDetails[0]);
				
				String movieName = movieDetails[1];
				
				Category movieType = Category.valueOf(movieDetails[2].toUpperCase());
				
				Language language = Language.valueOf(movieDetails[3].toUpperCase());
				
				Date releaseDate = format.parse(movieDetails[4]);
				
				List<String> casting = new ArrayList<String>();				
				for(String s : movieDetails[5].split("-")) {

					casting.add(s);
				}
				Double rating = Double.parseDouble(movieDetails[6]);
				
				Double totalBusinessDone = Double.parseDouble(movieDetails[7]);
				movies.add(new Movie(movieId, movieName, movieType, language, releaseDate, casting, rating, totalBusinessDone));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return movies;
	}
	
	public static Connection databaseConnection() {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String url = "jdbc:oracle:thin:@localhost:1521:XE";
		String username = "system";
		String password = "system";
		
		
		try {
			conn = DriverManager.getConnection(url, username, password);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return conn;
	}
	
	public Boolean allMovieInDb(List<Movie> movies) {
		int rows = 0;
		
		conn = databaseConnection();
//		System.out.println(conn);
		DateFormat format = new SimpleDateFormat("yyyy-mm-dd");
		for(Movie m : movies) {
			
			String sql = "INSERT INTO MOVIE_DETAILS VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			try {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, m.getMovieId());
				pstmt.setString(2, m.getMovieName());
				pstmt.setString(3, m.getMovieType().toString());
				pstmt.setString(4, m.getLanguage().toString());
//				System.out.println(format.format(m.getReleaseDate()));
				pstmt.setDate(5, java.sql.Date.valueOf(format.format(m.getReleaseDate())));
				String cast = "";
				for (String s : m.getCasting())
					cast += s + ",";
//				System.out.println(cast);
				pstmt.setString(6, cast);
				pstmt.setDouble(7, m.getRating());
				pstmt.setDouble(8, m.getTotalBusinessDone());
				
				rows += pstmt.executeUpdate();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (rows == movies.size())
			return true;
		else
			return false;
	}
	
	public List<Movie> addMovie(Movie movie,List<Movie> movies) {
		movies.add(movie);
		return movies;
	}
	
	public void serializeMovies(List<Movie> movies, String fileName) {
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		
		try {
			fout = new FileOutputStream(fileName, true);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(movies);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public List<Movie> deserializeMovie(String fileName) {
		ObjectInputStream ois = null;
		List<Movie> movies = null;
		try {
			FileInputStream fin = new FileInputStream(fileName);
			ois = new ObjectInputStream(fin);
			movies = (List<Movie>) ois.readObject();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return movies;
	}
	
	public List<Movie> getMoviesRealeasedInYear(int year) {
		ObjectInputStream ois = null;
		List<Movie> movies = null, movieInYear = new ArrayList<Movie>();
		DateFormat format = new SimpleDateFormat("yyyy");
		String fileName = "./src/javaAssignment09/output.ser";
		try {
			FileInputStream fin = new FileInputStream(fileName);
			ois = new ObjectInputStream(fin);
			movies = (List<Movie>) ois.readObject();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		for (Movie m : movies) {
			if ( format.format(m.getReleaseDate()).equalsIgnoreCase(Integer.toString(year)))
				movieInYear.add(m);
					
		}
		return movieInYear;
	}
	
	public List<Movie> getMoviesByActor(String... actorNames){
	ObjectInputStream ois = null;
	List<Movie> movies = null, movieByActor= new ArrayList<Movie>();
	DateFormat format = new SimpleDateFormat("yyyy");
	String fileName = "./src/javaAssignment09/output.ser";
	try {
		FileInputStream fin = new FileInputStream(fileName);
		ois = new ObjectInputStream(fin);
		movies = (List<Movie>) ois.readObject();
		
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		if (ois != null) {
			try {
				ois.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	for (Movie m : movies) {
		for(String actor : m.getCasting()) {
			for(String s : actorNames) {
				if (s.equalsIgnoreCase(actor))
					movieByActor.add(m);
			}
		}
				
	}
	return movieByActor;
}
		
	public void updateRatings(Movie movie, double rating ,List<Movie> movies) {
		movies.remove(movie);
		movie.setRating(rating);
		movies.add(movie);
		
	}
	
	public void updateBusiness(Movie movie, double amount,List<Movie> movies) {
		movies.remove(movie);
		movie.setTotalBusinessDone(amount);
		movies.add(movie);
		
	}
	
	public Map<Language, Set<Movie>> getMoviesByBusiness(List<Movie> movies, double amount)
	{
		Iterator<Movie> itr = movies.iterator();
		
		Map<Language, Set<Movie>> map = new HashMap<Language,Set<Movie>>();
		
		Set<Movie> english = new HashSet<Movie>();
		Set<Movie> hindi = new HashSet<Movie>();
		Set<Movie> japanese = new HashSet<Movie>();
		
		while(itr.hasNext()) {
			Movie movie = itr.next();
			
			if (movie.getLanguage().toString().equalsIgnoreCase("ENGLISH") && movie.getTotalBusinessDone() > amount) 
				english.add(movie);
			else if (movie.getLanguage().toString().equalsIgnoreCase("Hindi") && movie.getTotalBusinessDone() > amount)
				hindi.add(movie);
			else if (movie.getLanguage().toString().equalsIgnoreCase("japanese") && movie.getTotalBusinessDone() > amount)
				japanese.add(movie);
		}
		
		map.put(Language.ENGLISH, english);
		map.put(Language.HINDI, hindi);
		map.put(Language.JAPANESE, japanese);
		
		return map;
		
	}
}

public class MovieDriver {
	
	public static void main(String[] args) {
		Driver d = new Driver();
		System.out.println();
		
//		Populating from file
		File file = new File("./src/javaAssignment09/movieDetails.txt");
		List<Movie> movies = d.populateMovies(file);
		for(Movie mo : movies) {
			System.out.println(mo.getMovieName());
//			System.out.println(mo.getCasting());
		}
		System.out.println();	
		
//		Inserting to database
		if(d.allMovieInDb(movies))
			System.out.println("Data inserted!");
		else
			System.out.println("Error! Please try again.");
		System.out.println();
		
//		Adding new movie
		DateFormat format = new SimpleDateFormat("yyyy/mm/dd");
		List<String> cast = new ArrayList<String>();
		cast.add("Leonardo DiCaprio");
		cast.add("Matt Damon");
		cast.add("Jack Nicholson");
		try {
			Movie movie = new Movie(6, "The Departed", Category.valueOf("Thriller".toUpperCase()), Language.valueOf("ENGLISH".toUpperCase()), format.parse("2006/10/6"), cast, 8.5, 291465373.);
			movies = d.addMovie(movie, movies);
			for(Movie mo : movies) {
				System.out.println(mo.getMovieName());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		
		
////		Serialization of movies
//		d.serializeMovies(movies, "./src/javaAssignment09/output.ser");
//		System.out.println();
//		
////		Deserialization of movies
//		List<Movie> deMovies = d.deserializeMovie("./src/javaAssignment09/output.ser");
//		for(Movie mo : deMovies) {
//			System.out.println(mo.getMovieName());
//		}
//		System.out.println();
		
//		Finding all movies form particular year
		List<Movie> movieInYear = d.getMoviesRealeasedInYear(2020);
		for(Movie mo : movieInYear) {
			System.out.println(mo.getMovieName() + " " + mo.getReleaseDate());
		}
		System.out.println();
	
//		Finding all movies by actors
		List<Movie> movieByActor = d.getMoviesByActor("Leonardo DiCaprio");
		for(Movie mo : movieByActor) {
			System.out.println(mo.getMovieName() + " " + mo.getCasting());
		}
		System.out.println();
		
//		Update ratings of movie
		d.updateRatings(movies.get(0), 10, movies);
		System.out.println();
		for(Movie mo : movies) {
			System.out.println(mo.getMovieName() + " " + mo.getRating());
		}
		System.out.println();
		
//		Update total business done by movies
		d.updateBusiness(movies.get(0), 500000, movies);
		System.out.println();
		for(Movie mo : movies) {
			System.out.println(mo.getMovieName() + " " + mo.getTotalBusinessDone());
		}
		System.out.println();
		
//		Get business by amount
		Map<Language, Set<Movie>> map= d.getMoviesByBusiness(movies, 10.);
		for (Map.Entry<Language, Set<Movie>> entry : map.entrySet()) {
			for (Movie m : entry.getValue()) {
				System.out.println(entry.getKey() + " / " + m.getMovieName() + " " + m.getTotalBusinessDone());
			}
//			System.out.println(entry.getKey() + " / " + entry.getValue());
		}
		System.out.println();
		
	}

}
