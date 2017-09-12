package cst8284.assignment1;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Scanner;
	
public class MakeFile  {
	static String fileInputName = "BuildToDoList.txt";	
	static String fileOutputName = "D:\\ToDoList.todo";
	static File f = new File(fileInputName);
	static Date[] tdDate = {new Date(117, 2, 15), new Date(117, 3, 27), new Date(117, 2, 31), new Date(0, 0, 0)};

	public static void main(String[] arg) {
		ToDo[] tdArray = new ToDo[4]; 
		int k=0;
		
		try {
			Scanner input = new Scanner(f);
			while (input.hasNextLine()) {
				tdArray[k] = new ToDo();  // set creation date
				tdArray[k].setTitle(input.nextLine());
				tdArray[k].setSubject(input.nextLine());
				tdArray[k].setPriority(input.nextInt());
				tdArray[k].setCompleted(input.nextBoolean());
				tdArray[k].setRemove(input.nextBoolean());
				tdArray[k].setEmpty(input.nextBoolean());
				tdArray[k].setDueDate(tdDate[k]);
				k++;
				input.nextLine();
			}
			input.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try (
			FileOutputStream fos = new FileOutputStream(fileOutputName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
		) {
			for (int j = 0; j < tdArray.length; j++) 
				oos.writeObject(tdArray[j]);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
