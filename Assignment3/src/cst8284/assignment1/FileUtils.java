/*
 * TaskManager.java
 * Carlos Guillermo Rivera Negrete, 040877658
 * 17W_CST8284_300: 17W_CST8284_300 Object-Oriented Programming (Java)
 * Assignment 3
 * April 21, 2017
 * David B. Houtman
 */

package cst8284.assignment1;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javafx.scene.Scene;

/**
 * FileUtils is a class that will load elements from a .todo file to an ArrayList
 * of ToDo elements, it also does the opposite which is writting a file
 * from an ArrayList of ToDo elements.
 * 
 * @author Carlos Guillermo Rivera Negrete
 * @see Scene
 * @version 1.0, April 21, 2017
 * @since 1.0
 *
 */

public class FileUtils {
	private static String relPath = "ToDoList.todo";
	
	/**
	 * Creates an ArrayList of ToDo elements from a .todo file
	 * 
	 * @param fileName name of file to open
	 * @return ArrayList build by file elements
	 */
	public ArrayList<ToDo> getToDoArray(String fileName){
		ArrayList<ToDo> toDos = new ArrayList<ToDo>();
		File f = new File(fileName);
		setAbsPath(f);
		
		try(
				FileInputStream fin = new FileInputStream(fileName);
				ObjectInputStream ois = new ObjectInputStream(fin);
				){
			
			while(true){
				toDos.add((ToDo)ois.readObject());
			}
			
			}
			catch(EOFException e){
				System.out.println("Arrays of toDos has been transfered from file");
			}
			catch(FileNotFoundException e){
				System.out.println("File not found");
			}
			catch(ClassNotFoundException e){
				System.out.println("File not found");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		return toDos;
	}
		
	/**
	 * 
	 * @return value of relPath
	 */
	public static String getAbsPath() {
		return relPath;
	}

	/**
	 * 
	 * @param f file from which the absolute path will be taken
	 * @return absolute path as string from the file
	 */
	public static String getAbsPath(File f) {
		return f.getAbsolutePath();
	}

	/**
	 * 
	 * @param f file to set the new absolute path to
	 */
	public static void setAbsPath(File f) { 
		relPath = (fileExists(f))? f.getAbsolutePath():""; 
	}
	
	/**
	 * Return true if the file exists on the system, returns false if not.
	 * 
	 * @param f file to verify existence
	 * @return true or false as a boolean
	 */
	public static Boolean fileExists(File f) {
		return (f != null && f.exists() && f.isFile() && f.canRead());
	}
	
	/**
	 * Writes on a file based on the elements of the ArrayList of ToDos
	 * 
	 * @param toDoArray ArrayList to use elements from
	 * @param fileName file to write on
	 */
	public static void setToDoArrayListToFile(ArrayList<ToDo> toDoArray, String fileName){
		try(
				FileOutputStream fout = new FileOutputStream(fileName);
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				){
			for (ToDo todo: toDoArray)
				oos.writeObject(todo);
			}
			catch(FileNotFoundException e){
				System.out.print("File not found" + fileName);
			}
			catch(IOException e){
				e.printStackTrace();
			}
	}

}
