public class Main {
    public static void main(String[] args){
        Person p1 = new Person("Aisha", "P001");
        Person p2 = new Lecturer("Sara", "L001");
        Person p3 = new Student("Ahmed", "S001");

        p1.introduce();
        p2.introduce();
        p3.introduce();
    }
}