import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import java.time.Duration;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;




public class automacao {

    WebDriver navegar;

    WebDriverWait wait;

    @Before
    public void setup() {
        navegar = new EdgeDriver();
        wait = new WebDriverWait(navegar, Duration.ofSeconds(20));

        System.setProperty("webdriver.edge.driver", "src/drive/msedgedriver");
        
        navegar.get("http://localhost:4200/web/instructor");
        
        navegar.findElement(By.xpath("//*[@id='btn-login']")).click();
        
        navegar.get("http://localhost:4200/web/instructor/sessions");

        navegar.findElement(By.xpath("//*[@id='btn-add-session']")).click();
    }

    // Esperar elemento aparecer na tela
    private WebElement waitForElementVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    //------------------------------------------------------------
    // Choose a session type
    public boolean selectSessionType(int option){
        
        WebElement elementType = waitForElementVisibility(By.xpath("//*[@id='session-type']"));
        
        //Se n for a opção de copiar sessão
        if (option != 3){
            WebElement sessionName = waitForElementVisibility(By.xpath("//*[@id='add-session-name']"));
        
            sessionName.click();
            sessionName.sendKeys("teste" + Integer.toString(option));

            Select select = new Select(elementType);
            select.selectByIndex(option);

            WebElement createSession = navegar.findElement(By.xpath("//*[@id='btn-create-session']"));

            createSession.click();

            try {
                waitForElementVisibility(By.xpath("//*[@id='main-content']/div/tm-instructor-session-edit-page/tm-loading-retry[2]/div[2]"));
                return true;
            } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
                return false;
            }
        }
        
        WebElement buttonCopy = navegar.findElement(By.xpath("//*[@id='btn-copy-session']/span"));

        buttonCopy.click();

        try {
            waitForElementVisibility(By.xpath("/html/body/ngb-modal-window/div/div"));
            return true;
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            return false;
        }


    }

    @Test
    public void ownQuestion(){
        Assert.assertFalse(selectSessionType(2));
    }

    @Test
    public void templateQuestion(){
        Assert.assertTrue(selectSessionType(0));
    }

    @Test
    public void previousQuestion(){
        Assert.assertTrue(selectSessionType(3));
    }

    //------------------------------------------------------------
    //Course ID

    public boolean CheckEnrolledCourse(Map<String, String> courses){
        Map<String, String> expectedCourses = new HashMap<>();

        //courseId e courseName
        expectedCourses.put("432", "Engenharia Eletrônica");
        expectedCourses.put("1234", "Engenharia de Software");
        expectedCourses.put("sidney.gma-demo", "Sample Course 101");

        try {
            Assert.assertEquals(expectedCourses, courses);
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    @Test
    public void courseID(){

        Map<String, String> courses = new HashMap<>();
        
        WebElement elementId = waitForElementVisibility(By.xpath("//*[@id='add-course-id']"));

        List<WebElement> coursesId = elementId.findElements(By.tagName("option"));
    

        WebElement courseNameDiv = navegar.findElement(By.xpath("//*[@id='course-name']"));


        for (WebElement courseId : coursesId) {
            courseId.click();  //seleciona elemento
            String courseIdText = courseId.getText();
            String courseName = courseNameDiv.getText();
            courses.put(courseIdText, courseName);
        }
        
        for (Map.Entry<String, String> entry : courses.entrySet()) {
            String courseIdText = entry.getKey();
            String courseName = entry.getValue();
            System.out.println("CourseId: " + courseIdText + ", Course Name: " + courseName);
        }

        if(!CheckEnrolledCourse(courses)){
            Assert.fail("CourseID and Course Name don't match");
        }

    }
    //------------------------------------------------------------
    //Session name

    public boolean insertSessionname(String sessionName){

        WebElement element = waitForElementVisibility(By.xpath("//*[@id='add-session-name']"));

        String text = " is not acceptable to TEAMMATES as a/an feedback session name because it starts with a non-alphanumeric character. A/An feedback session name must start with an alphanumeric character, and cannot contain any vertical bar (|) or percent sign (%).";

        String emptyName = "Session name cannot be empty";

        element.click();
        element.sendKeys(sessionName);
        
        navegar.findElement(By.xpath("//*[@id='btn-create-session']")).click();

        try {
            WebElement errorElement = waitForElementVisibility(By.xpath("/html/body/tm-root/tm-instructor-page/tm-page/tm-toast/ngb-toast/div"));
            
            String errorTexto = errorElement.getText();
            
            if(errorTexto.contains('"' + sessionName + '"' + text)){
                return true;
            }
            else if(errorTexto.contains(emptyName)){
                return true;
            }

        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }

        return false;
    }

    @Test
    public void smallerOne(){
        Assert.assertTrue(insertSessionname(""));
    }

    @Test
    public void over64(){
        String name = "Esteehumtextoquefoiescritoequepossuimaisdesessentaequatrocaracteres";
        WebElement element = waitForElementVisibility(By.xpath("//*[@id='add-session-name']"));

        element.click();
        element.sendKeys(name);

        String typedName = element.getAttribute("value");
        int countKey = typedName.length();

        Assert.assertFalse("session name maior que 64 caracteres", countKey > 64);

    }

    @Test
    public void validSessionName(){
        Assert.assertFalse(insertSessionname("teste1"));
    }

    @Test
    public void invalidSessionName_1(){
        Assert.assertTrue(insertSessionname("@teste"));  
    }

    @Test
    public void invalidSessionName_2(){
        Assert.assertTrue(insertSessionname("|teste"));  
    }

    // @Test
    // public void cleanupAndTeardown(){
    //     navegar.navigate().to("http://localhost:4200/web/instructor/sessions");

    //     while (true) {
    //         try {
    //             WebElement buttonElement = waitForElementVisibility(By.xpath("//*[@id='main-content']/div/tm-instructor-sessions-page/tm-loading-retry/div[1]/div/tm-sessions-table/div/table/tbody/tr[1]/td[6]/div/button[1]"));
    //             buttonElement.click();

    //             WebElement yesButton = waitForElementVisibility(By.xpath("/html/body/ngb-modal-window/div/div/tm-confirmation-modal/div[4]/button[2]"));
    //             yesButton.click();

    //         } catch (NoSuchElementException | TimeoutException e) {
    //             break;  // Sai do loop quando o elemento não for encontrado
    //         }
    //     }
        
    //     try{
    //         WebElement deleteAll = waitForElementVisibility(By.xpath("//*[@id='btn-delete-all']"));
    //         deleteAll.click();

    //         WebElement confirmButton = waitForElementVisibility(By.xpath("/html/body/ngb-modal-window/div/div/tm-sessions-permanent-deletion-confirm-modal/div[3]/button[2]"));
    //         confirmButton.click();
    //     } catch (NoSuchElementException e){

    //     }

        
    // }
}
