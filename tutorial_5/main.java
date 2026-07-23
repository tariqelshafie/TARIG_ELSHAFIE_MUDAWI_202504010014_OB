public class main{
    public static void main(String[]args){
        Student s = new Student();
        s.setStudentID("CU12345");
        s.setName("Tariq");
        s.setCGPA(4.00);
        s.setProgramme("BCSSE");

        System.out.println("Student ID : "+ s.getStudentID());
        System.out.println("Name       : "+ s.getName());
        System.out.println("CGPA       : "+ s.getCGPA());
        System.out.println("Programme  : "+ s.getProgramme());
    }
}