package logic;

import data.Course;
import data.Group;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class AutoSIA {
    private static final String url = "https://sia.unal.edu.co/ServiciosApp/facespublico/public/" +
            "servicioPublico.jsf?taskflowId=task-flow-AC_CatalogoAsignaturas";
    private static final String mostrarButton = "pt1:r1:0:cb1";
    private static final String volverButton = "Volver";
    private static final String tableClass = "af_table_data-body";

    private static HashMap<String, Course> courses = new HashMap<>();
    private static ArrayList<String> seenCourses = new ArrayList<>();

    private WebDriverWait wait;
    private WebDriver driver;

    // TODO: Make it available for all faculties
    private String[] IDs = {"pt1:r1:0:soc1::content", "pt1:r1:0:soc2::content", "pt1:r1:0:soc3::content"};
    private String[] texts;

    public AutoSIA(String degree){
        this.setDriver();
        this.setWait();
        this.texts = new String[]{"Pregrado", "2055 FACULTAD DE INGENIERÍA", degree};
    }

    public static void setCourses(HashMap<String, Course> savedData){
        courses = savedData;
    }

    private void setDriver(){
        // Set path to driver and set driver
        System.setProperty("webdriver.gecko.driver", "lib/geckodriver.exe");
        this.driver = new FirefoxDriver();
    }

    private void setWait(){
        // Set wait object
        long seconds = 30;
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(seconds));
    }

    public void getInfo(){
        //TODO: delete this
        this.driver.manage().window().setSize(new Dimension(900, 800));

        // Open Firefox at the given url
        this.driver.get(url);

        // Fill each form and select the desired degree
        this.getDegree();

        // Get the table with Courses each degree has
        this.getTableCourses();

        // Close windows
        this.driver.quit();
    }

    private void getDegree(){
        for (int i = 0; i < this.IDs.length; i++){
            WebElement element = this.driver.findElement(By.id(IDs[i]));
            Select dropdown = new Select(element);
            dropdown.selectByVisibleText(texts[i]);
            this.wait.until(ExpectedConditions.stalenessOf(element));
        }
    }

    private void getTableCourses(){
        // Get table with all the Courses
        WebElement button = this.driver.findElement(By.id(mostrarButton));
        button.click();
        WebElement info = this.driver.findElement(By.id("pt1:r1:0:pb3"));
        this.wait.until(ExpectedConditions.stalenessOf(info));

        // Get how many courses the degree has
        WebElement tableB = this.driver.findElement(By.className(tableClass));
        int numCourses = tableB.findElements(By.tagName("a")).size();

        for (int i = 0; i < numCourses; i++){
            // Get link of each course and click on it
            WebElement table = this.driver.findElement(By.className(tableClass));
            List<WebElement> courses = table.findElements(By.tagName("a"));
            WebElement[] coursesArray = courses.toArray(new WebElement[0]);
            String code = coursesArray[i].getText();

            // If the course's info has already been seen, omit it
            if (courseSeen(code)){
                continue;
            }

            // Click on the Course and save its info
            courses.get(i).click();
            getCourseInfo(code);
        }
    }

    private static synchronized boolean courseSeen (String code){
        // Course has already been seen by other thread, no need to check it again
        if (seenCourses.contains(code)){
            return true;
        }

        // Get the course info since it is not in the main list
        seenCourses.add(code);
        return false;
    }

    private void getCourseInfo(String code){
        // Wait until the page is full loaded
        this.wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText(volverButton)));

        // Get the number of groups, name and each group
        String fullName = this.driver.findElement(By.tagName("h2")).getText();
        List<WebElement> groupsInfolist = this.driver.findElements(By.className("af_showDetailHeader_content0"));
        List<WebElement> numGroupsList = this.driver.findElements(By.className("af_showDetailHeader_title-text-cell"));

        // Change from list to array
        WebElement[] groupsInfo = groupsInfolist.toArray(new WebElement[0]);
        ArrayList<WebElement> numGroups = new ArrayList<>(numGroupsList);

        // If the Course has no groups, omit it
        if (groupsInfo.length == 0){
            returnMainPage();
            return;
        }

        // Omit the Course's syllabus
        if (numGroups.get(0).getText().equals("Contenido de la asignatura")){
            numGroups.remove(0);
        }

        // Check if the course is already at the main list
        Course course = isCourseSaved(code);

        if (course == null){
            // Create a node with the course's code and name, and a queue for the groups
            String name = fullName.substring(0, fullName.indexOf("("));
            course = new Course(code, name);
            Queue<Group> data = new LinkedList<>();
            course.setGroups(data);

            // Add course to the main list
            addCourse(course);
        }

        // Get the info of each group
        for (int i = 0; i < groupsInfo.length; i++){
            // Get group's number
            String numGroupLong = Arrays.asList(numGroups.get(i).getText().split(" ")).get(0);
            String numGroup = numGroupLong.substring(1, numGroupLong.length()-1);

            // Get teacher's name and number of spots
            List<String> info = Arrays.asList(groupsInfo[i].getText().split("\n"));
            String[] infoArray = info.toArray(new String[0]);
            String teacher = Arrays.asList(infoArray[0].split(": ")).get(1);
            String spots = Arrays.asList(infoArray[info.size()-1].split(": ")).get(1);

            // If there are no more available spots
            if (satisfyCondition(spots)){
                Group groupInfo = isGroupSaved(course, numGroup);

                // Save the group if it is not already in the course's queue
                if (groupInfo == null) {
                    Group newGroup = new Group(numGroup, teacher);
                    course.getGroups().add(newGroup);
                }

                // If the teachers name isn't known, update it
                else if (groupInfo.getTeacher().equals("No informado")){
                    groupInfo.setTeacher(teacher);
                }
            }
        }

        // Return to main page with the rest of Courses
        returnMainPage();
    }

    private static synchronized Course isCourseSaved(String code){
        // Returns the course if it is on the list, null otherwise
        return courses.get(code);
    }

    private static synchronized Group isGroupSaved(Course course, String numGroup){
        // Returns true if the group is already inside the course's queue
        if (course.getGroups().contains(numGroup)){
            return course.getGroups().peek();
        }
        return null;
    }

    private static synchronized void addCourse(Course course){
        // Adds course to the main list
        courses.put(course.getCourseCode(), course);
    }

    private boolean satisfyCondition(String spots){
        if (spots != null && spots.length() == 1){
            return spots.charAt(0) == '0';
        }
        return false;
    }

    private void returnMainPage(){
        // Click in "volver" button
        this.driver.findElement(By.linkText(volverButton)).click();

        // Make sure you are in the main page. If not, click again in "Volver"
        if (!isOnMainPage()){
            returnMainPage();
        }
    }

    private boolean isOnMainPage(){
        // Returns true if webpage is on the main page
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className(tableClass)));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static HashMap<String, Course> getCourses(){
        return courses;
    }
}
