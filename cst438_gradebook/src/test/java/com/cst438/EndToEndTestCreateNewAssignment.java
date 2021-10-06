package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;

@SpringBootTest
public class EndToEndTestCreateNewAssignment
{
   public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver_win32/chromedriver.exe";

   public static final String URL = "https://jt-cst438-fe.herokuapp.com/";
   public static final String TEST_USER_EMAIL = "test@csumb.edu";
   public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
   public static final int SLEEP_DURATION = 500; // 0.5 seconds
   public static final int TEST_COURSE_ID = 123456;

   @Autowired
   EnrollmentRepository enrollmentRepository;

   @Autowired
   CourseRepository courseRepository;

   @Autowired
   AssignmentGradeRepository assignnmentGradeRepository;

   @Autowired
   AssignmentRepository assignmentRepository;

   @Test
   public void createNewAssignment() throws Exception {
      
//    Database setup:  create course      
      Course c = new Course();
      c.setCourse_id(TEST_COURSE_ID);
      c.setInstructor(TEST_INSTRUCTOR_EMAIL);
      c.setSemester("Fall");
      c.setYear(2021);
      c.setTitle("Test Course");

//     add an assignment that needs grading for course TEST_COURSE_ID
      Assignment a = new Assignment();
      a.setCourse(c);
      // set assignment due date to 24 hours ago
      a.setDueDate(new java.sql.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
      a.setName("TEST ASSIGNMENT");
      a.setNeedsGrading(1);

//     add a student TEST into course 99999
      Enrollment e = new Enrollment();
      e.setCourse(c);
      e.setStudentEmail(TEST_USER_EMAIL);
      e.setStudentName("Test");

      //courseRepository.save(c);
      a = assignmentRepository.save(a);
      //e = enrollmentRepository.save(e);

      AssignmentGrade ag = null;

      // set the driver location and start driver
      //@formatter:off
      // browser  property name           Java Driver Class
      // edge  webdriver.edge.driver      EdgeDriver
      // FireFox  webdriver.firefox.driver   FirefoxDriver
      // IE       webdriver.ie.driver     InternetExplorerDriver
      //@formatter:on

      System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
      WebDriver driver = new ChromeDriver();
      // Puts an Implicit wait for 10 seconds before throwing exception
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

      driver.get(URL);
      Thread.sleep(SLEEP_DURATION);

      try {
         // locate input element for assignment for 'Test Course'
         //WebElement we = driver.findElement(By.xpath("//div[@data-value='TEST ASSIGNMENT']//input"));
         //we.click();

         // Locate and click Go button
         //driver.findElement(By.xpath("//a")).click();
         //Thread.sleep(SLEEP_DURATION);
         
//         WebElement we = driver.findElement(By.xpath("//div[@name='assignmentBody']"));
//         we.findElement(By.xpath("following-sibling::input[@name='assignmentName']")).sendKeys("E2E Assign1");
//         we.findElement(By.xpath("following-sibling::input[@name='assignmentDueDate']")).sendKeys("12/21/21 11:59pm");
//         we.findElement(By.xpath("following-sibling::input[@name='assignmentName']")).sendKeys("123456");
//         
         WebElement we = driver.findElement(By.id("assignmentBody"));
         we.findElement(By.id("assignmentName")).sendKeys("E2E Assign1");
         we.findElement(By.id("assignmentDueDate")).sendKeys("12/21/21 11:59pm");
         we.findElement(By.id("courseId")).sendKeys("123456");
         
         // Locate row for student name "Test" and enter score of "99.9" into the grade field
         //we = driver.findElement(By.xpath("//div[@data-field='name' and @data-value='Test']"));
         //we.findElement(By.xpath("following-sibling::div[@data-field='grade']")).sendKeys("99.9");

         // Locate submit button and click
         driver.findElement(By.xpath("//button[@name='buttonSubmit']")).click();
         Thread.sleep(SLEEP_DURATION);

         //verify that score show up
         //we = driver.findElement(By.xpath("//div[@data-field='name' and @data-value='Test']"));
         //we =  we.findElement(By.xpath("following-sibling::div[@data-field='grade']"));
         //assertEquals("99.9", we.getAttribute("data-value"));
         we = driver.findElement(By.xpath("//div[@data-value='E2E Assign1']"));
         assertEquals("E2E Assign1", we.getAttribute("data-value"));
         
         //verify that assignment_grade has been added to database with score of 99.9
         //ag = assignnmentGradeRepository.findByAssignmentIdAndStudentEmail(a.getId(), TEST_USER_EMAIL);
         //assertEquals("99.9", ag.getScore());
         
         // there is no clean way to get the last assignment added since we don't have a function for it
         List<Assignment> assigns = assignmentRepository.findNeedGradingByEmail("dwisneski@csumb.edu");
         for(Assignment assign: assigns) {
            if(assign.getName() != "E2E Assign1") {
               continue;
            }
            else if(assign.getName() == "E2E Assign1") {
               assertEquals("E2E Assign1", assign.getName());
               break;
            }          
            // fail fall through case
            assertEquals("E2E Assign1", assign.getName());
         }
         
      } catch (Exception ex) {
         throw ex;
      } finally {

         // clean up database.
         ag = assignnmentGradeRepository.findByAssignmentIdAndStudentEmail(a.getId(), TEST_USER_EMAIL);
         if (ag!=null) assignnmentGradeRepository.delete(ag);
         enrollmentRepository.delete(e);
         assignmentRepository.delete(a);
         courseRepository.delete(c);

         driver.quit();
      }
   }
   
}// end class
