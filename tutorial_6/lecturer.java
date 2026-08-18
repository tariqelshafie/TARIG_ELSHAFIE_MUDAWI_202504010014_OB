public class lecturer extends employee {

    private String subject;
    private String department;

    public lecturer(String id, String name, String subject, String department) {
        super(id, name);
        this.subject = subject;
        this.department = department;
    }

    public void displaySubject() {
        System.out.println("Subject     : " + subject);
    }

    public void displayDepartment() {
        System.out.println("Department    : " + department);
    }
}