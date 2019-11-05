package gw;
/*
 * Copyright 2014 - 2018 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/*
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
 */

//import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cognizant.cognizantits.engine.commands.General;
import com.cognizant.cognizantits.engine.commands.WaitFor;
import com.cognizant.cognizantits.engine.constants.ObjectProperty;
import com.cognizant.cognizantits.engine.constants.SystemDefaults;
import com.cognizant.cognizantits.engine.core.CommandControl;
import com.cognizant.cognizantits.engine.drivers.WebDriverFactory.Browser;
import com.cognizant.cognizantits.engine.execution.exception.ForcedException;
import com.cognizant.cognizantits.engine.execution.exception.element.ElementException;
import com.cognizant.cognizantits.engine.execution.exception.element.ElementException.ExceptionType;
import com.cognizant.cognizantits.engine.support.Status;
import com.cognizant.cognizantits.engine.support.methodInf.Action;
import com.cognizant.cognizantits.engine.support.methodInf.InputType;
import com.cognizant.cognizantits.engine.support.methodInf.ObjectType;

public class Guidewire extends General {

	public Guidewire(CommandControl cc) {
		super(cc);
	}

	public static int gRowIndex = 0;
	private static int DEFAULT_WAIT_TIME, DEFAULT_ELEMENT_WAIT_TIME;
	private static int waitTime = DEFAULT_WAIT_TIME;
	int SYNC_WAIT_MILLISECONDS = 500;
	By LOADING_DIV = By.cssSelector("div.gw-click-overlay.gw-disable-click");
	By LOADING_DIV_IG = By
			.cssSelector("[id='igProgress'][style^='display:block'],[id='igProgress'][style^='display: block']");

	// By LOADING_DIV = By.cssSelector("div.gw-click-overlay.gw-disable-click");
	By byFocusInput = By.xpath(
			"//input[(contains(@id,'text') and contains(@id,'-inputEl')) or (contains(@id,'simple') and contains(@id,'-inputEl')) or (contains(@class,'x-field-form-focus'))]");

	By BTN_OK_Popup = By.xpath("//span[contains(@id,'button-') and contains(@id,'-btnInnerEl') and text()='OK']");

	private static final By IMG_LOGO = By.xpath("//*[@class='x-img product-logo x-box-item x-img-default']");

	@Action(object = ObjectType.SELENIUM, desc = "Build and Enter a random string", input = InputType.YES, condition = InputType.OPTIONAL)
	public void GWAppendDynamicStringEnter() {
		try {
			boolean bUseDataFromExcel = true;
			String strData = Data;
			// Check if input is not empty
			if (!Data.isEmpty()) {
				// Check if variable name is provided to use run time data
				if (!Condition.isEmpty() && Condition != null) {
					if (Condition.startsWith("%") && Condition.endsWith("%")) {
						Data = this.getUserDefinedData(Condition);
						if (Data != null) {
							if (!Data.isEmpty()) {
								Report.updateTestLog(Action, "Using runtime value '" + Data + "'", Status.PASS);
								bUseDataFromExcel = false;
							}
						}
					} else {
						// If variable name is provided but not in correct
						// format, throw warning and use user defined data
						Report.updateTestLog(Action,
								"Varibale mapped under condition column is not in the correct format and hence continuing the test with value given by user",
								Status.WARNING);
					}
				}
				if (bUseDataFromExcel) {
					Data = strData;
					// Check if EXACT keyword is used, if yes, user input will
					// be used removing EXACT keyword
					if (Data.contains("(EXACT)")) {
						String DataToBeEntered = Data;
						DataToBeEntered = DataToBeEntered.replace("(EXACT)", "");
						Data = DataToBeEntered;
					} else {
						// If EXACT keyword is not used, add 4 random chars to
						// end
						Random rand = new Random();
						String strcharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
						String strRandomString = "";
						// String strLastName = "";
						for (int i = 1; i < 5; i++) {
							int randomNum1 = rand.nextInt(25) + 0;
							strRandomString = strRandomString + strcharacters.substring(randomNum1, randomNum1 + 1);
						}
						Data = Data + strRandomString;
					}
				}
				// Enter text in input/text box
				executeMethod(AObject.findElement(ObjectName, Reference), "Set", Data);
			} else {
				Report.updateTestLog(Action,
						"Input provided is blank/invalid. No data being entered in Object '" + ObjectName + "'",
						Status.WARNING);
			}
		} catch (Exception ex) {
			Report.updateTestLog(Action, "Exception '" + ex.getMessage() + "' returned", Status.DEBUG);
		}
	}

	/**
	 * To set Value in Edit box by appending random text
	 * 
	 * @throws InterruptedException
	 */

	@Action(object = ObjectType.SELENIUM, desc = "Enter Random Text in the Field [<Object>]", condition = InputType.OPTIONAL,input = InputType.OPTIONAL)
	public void GWDynamicStringEnter() throws InterruptedException {
		String strData="";
		try {
			int length = 10;
			if (!Condition.isEmpty() && Condition != null) {
				length = Integer.parseInt(Condition);
			}

			strData = Data + generateRandomString(length);


			waitforElementPresent(Element);
			if (Element.isEnabled()) {
				Element.clear();
				sync();
				Element.sendKeys(strData);
				sync();
				clickLogo();
				sync();
				Report.updateTestLog(Action, "Entered Text '" + strData + "' in '" + ObjectName + "'", Status.PASS);
			} else {
				Report.updateTestLog(Action, "Element '" + ObjectName + "' not Enabled/Visible after '"
						+ SystemDefaults.waitTime.get() + "' Seconds ", Status.FAIL);
			}
		} catch (Exception ex) {
			Report.updateTestLog(Action,
					"Exception while entering '" + strData + "' in '" + ObjectName + "'" + ex.getMessage(), Status.FAIL);
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			} else {
				throw new ForcedException(Action, ex.getMessage());
			}
		}
	}


	public String generateRandomString(int targetStringLength) {
		// Random string generation
		int leftLimit = 65; // letter 'A'
		int rightLimit = 90; // letter 'Z'
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		String generatedString = buffer.toString();
		return generatedString;
	}

	@Action(desc = "Generate Date from [<Data>] and Condition and Enter in the Field [<Object>] or into Variable %CalcDate%", input = InputType.YES)
	public void GWEnterDate() throws InterruptedException, ParseException {
		String clockDate = "";
		try {

			SimpleDateFormat dtFrmt = new SimpleDateFormat("MM/dd/yyyy");
			int flag = 0;
			Date today = new Date();

			if (!Data.isEmpty() && Data != null) {

				if (Data.contains(";")) {
					clockDate = Data.split(";")[0];
					today = dtFrmt.parse(clockDate);
				} else if (Data.contains("/")) {
					clockDate = Data;
					today = dtFrmt.parse(clockDate);
				} else {
					clockDate = Data;

					if (Condition != null && !Condition.isEmpty()) {
						if (Condition.contains(":")) {
							// String strCurrentDateFromServer =
							// Driver.findElement(By.xpath(Condition)).getText().trim();
							String sheetName = Condition.split(":", 2)[0];
							String columnName = Condition.split(":", 2)[1];
							String input = userData.getData(sheetName, columnName);
							if (input != null && !input.isEmpty()) {
								today = dtFrmt.parse(input);
							}
						} else if (Condition.contains("%")) {
							String input = getVar(Condition);
							if (input != null && !input.isEmpty()) {
								today = dtFrmt.parse(input);
							}
						} else {
							Report.updateTestLog(Action,
									"Given Data Sheet [" + Condition
									+ "] format is invalid. It should be [SheetName:ColumnName]. Continuing with System Date as Today",
									Status.FAIL);
						}
					}
					flag = 1;
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(today);
				if (flag == 1) {
					calendar.add(Calendar.DATE, Integer.parseInt(clockDate));
				}

				Date ExpectedDate = calendar.getTime();
				DateFormat dtFormat = new SimpleDateFormat("MM/dd/yyyy");
				Data = dtFormat.format(ExpectedDate);

				if (ObjectName.equalsIgnoreCase("Browser")) {
					addVar("%CalcDate%", Data);
					Report.updateTestLog(Action, "Calculated Date [" + Data + "] Stored into variable %CalcDate%",
							Status.DONE);
				} else {
					// Element = AObject.findElement(ObjectName, Reference);
					Element = AObject.findElement(ObjectName, Reference);
					new WaitFor(getCommander()).waitForElementToBePresent();
					if (Element.isEnabled()) {
						if (!Element.getAttribute("value").equalsIgnoreCase(Data)) {
							Element.clear();
							Element.sendKeys(Data);
							System.out.println("Entered Date is " + Data);
							executeMethod(Element, "waitForAngularRequestsToFinish");
						}
						Report.updateTestLog(Action, "Generated Date: '" + Data + "' from given text '" + clockDate
								+ "' and entered in '" + ObjectName + "'", Status.PASS);
					} else {
						Report.updateTestLog(Action,
								"Element '" + ObjectName + "' is not enabled to enter date '" + Data + "'",
								Status.FAIL);
						throw new ElementException(ExceptionType.Element_Not_Enabled, ObjectName);
					}
				}
			}
		} catch (Exception ex) {
			Report.updateTestLog(Action,
					"Could not generate Date from entered Text: '" + clockDate + "'. Details:" + ex.getMessage(),
					Status.FAIL);
			if (ex instanceof InterruptedException) {
				// Thread.currentThread().interrupt();
			}
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "Convert the Date from <dd/MM/yyyy> to <MM/dd/yyyy> with the date given in <INPUT>", input = InputType.YES)
	public void GWSetDate() throws InterruptedException, ParseException {

		try {
			System.out.println("Date:" + Data);
			SimpleDateFormat dtFrmt = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat GWdtFrmt = new SimpleDateFormat("MM/dd/yyyy");
			Date date1 = new Date();
			date1 = dtFrmt.parse(Data);
			String date2 = GWdtFrmt.format(date1);

			System.out.println("Formatted Date:" + date2);

			executeMethod(Element, "Set", date2);
			// Report.updateTestLog(Action, "Entered '" + Data + " in field " +
			// ObjectName, Status.PASS);

		} catch (Exception ex) {
			Report.updateTestLog(Action,
					"Could not generate Date from entered Text: '" + Data + "'. Details:" + ex.getMessage(),
					Status.FAIL);
			if (ex instanceof InterruptedException) {
				// Thread.currentThread().interrupt();
			}
		}
	}

	@Action(desc = "Logout from application")
	public void GWLogOut() throws InterruptedException {
		try {
			Thread.sleep(500);
			Driver.findElement(By.xpath("//*[@id=':TabLinkMenuButton-btnIconEl']")).click();
			// executeMethod(Element,"waitForAngularRequestsToFinish");
			Thread.sleep(500);
			WebElement Logout = Driver.findElement(By.xpath("//*[@id='TabBar:LogoutTabBarLink-textEl']"));
			if (Logout != null) {
				Logout.click();
				executeMethod(Element, "waitForAngularRequestsToFinish");
				String UnSavedWorkPopUp = "//*[@class='x-window x-message-box x-layer x-window-default x-closable x-window-closable x-window-default-closable x-border-box']";
				String CloseButton = "//*[@class='x-window x-message-box x-layer x-window-default x-closable x-window-closable x-window-default-closable x-border-box']/div[3]/div/div/a[1]/span/span";
				List<WebElement> UnSavedPopUpWindow = Driver.findElements(By.xpath(UnSavedWorkPopUp));
				if (!UnSavedPopUpWindow.isEmpty()) {
					WebElement CloseBtn = Driver.findElement(By.xpath(CloseButton));
					if (CloseBtn != null) {
						CloseBtn.click();
						Report.updateTestLog(Action, "Unsaved work Popup window handled", Status.DONE);
						Thread.sleep(1000);
					}
				}
				Report.updateTestLog(Action, "Logged Out from application successfully", Status.PASS);
			} else {
				Report.updateTestLog(Action, "Log Out Link is not available", Status.FAIL);
			}
		} catch (Exception ex) {
			Report.updateTestLog(Action, "Exception while logging out. Details: '" + ex.getMessage() + "'",
					Status.DEBUG);
			if (ex instanceof InterruptedException) {
				Report.updateTestLog(Action, "Exception that interrupts. Details:", Status.DEBUG);
				Thread.currentThread().interrupt();
			}
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "Using xPath identify the link/button available for object specified in [<Data>] and Click it", input = InputType.YES)
	public void GWRandomClick() throws InterruptedException {
		try {
			// Getting input value
			String strName = Data;
			String findValue = "";
			String strWebElementColXpath = AObject.getObjectProperty(Reference, ObjectName, ObjectProperty.Xpath);

			if (strName.contains(":")) {
				String sheetName = strName.split(":")[0];
				String columnName = strName.split(":")[1];
				findValue = userData.getData(sheetName, columnName);
			} else if (strName.contains("%")) {
				findValue = getVar(strName);
			} else {
				findValue = Data;
			}

			if (strWebElementColXpath.contains("##randomString##")) {
				strWebElementColXpath = strWebElementColXpath.replace("##randomString##", findValue.trim());
			}

			Thread.sleep(2000);
			sync();

			WebElement Element = Driver.findElement(By.xpath(strWebElementColXpath));
			executeMethod(Element, "waitForElementToBePresent");

			((JavascriptExecutor) Driver).executeScript("arguments[0].style.border='3px solid red'",
					Driver.findElement(By.xpath(strWebElementColXpath)));

			// Element.click();
			clickByJS(By.xpath(strWebElementColXpath));
			Thread.sleep(2000);

			// Handle HTTP request popup
			clickOkPopupifExists();

			executeMethod(Element, "waitForAngularRequestsToFinish");

			Report.updateTestLog(Action, "Clicked on '" + ObjectName + "' in row matching '" + Data + "'", Status.PASS);
		} catch (Exception ex) {
			Report.updateTestLog(Action, "Exception " + ex.getMessage() + " returned", Status.FAIL);
			if (ex instanceof InterruptedException) {
				Report.updateTestLog(Action, "Interrupt Exception " + ex.getMessage() + " returned", Status.FAIL);
				Thread.currentThread().interrupt();
			}
		}
	}

	@Action()
	public void GWScreenNavigation() {
		try {
			// Wait for Element to be present
			executeMethod(Element, "waitForElementToBePresent");
			// Create New reference for the same element
			Element = AObject.findElement(ObjectName, Reference);
			// AObject.setWaitAndFind(false);

			if (Element == null) {
				// Capture Error Message and set indicator to Stop the current
				// iteration
				String XPathOfErrorMessage = "(//*[@class='message'])";
				String ActualErrormessage = Driver.findElement(By.xpath(XPathOfErrorMessage)).getText();
				Report.updateTestLog(ObjectName, "Received Error while navigation - '" + ActualErrormessage
						+ "'; Current iteration interrupted; " + "Proceeding with next iteration, if applicable.",
						Status.FAIL);
				SystemDefaults.stopCurrentIteration.set(true);
			}
		} catch (Exception ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "Enter the value [<Data>] in the Field [<Object>]", input = InputType.YES)
	public void GWSet() throws InterruptedException {
		if (Data != null && !Data.isEmpty()) {
			try {

				// To get the Reference cell value
				Data = referenceVal(Data);

				if (Data.contains("$")) {
					Data = Data.replace("$", "");
				}
				if (Data.contains(",")) {
					Data = Data.replace(",", "");
				}
				if (Data.contains("(")) {
					Data = Data.replace("(", "-");
				}
				if (Data.contains(")")) {
					Data = Data.replace(")", "");
				}

				executeMethod(Element, "waitForElementToBeVisible");
				AObject.findElement(ObjectName, Reference);
				if (Element.isEnabled()) {
					Element.clear();
					executeMethod(Element, "waitForAngularRequestsToFinish");
					sleep(1000);
					Element = AObject.findElement(ObjectName, Reference);
					Element.sendKeys(Data);
					clickLOGO();
					sync();

					Report.updateTestLog(Action, "Entered Text '" + Data + "' in '" + ObjectName + "'", Status.PASS);
				} else {
					Report.updateTestLog(Action, "Element '" + ObjectName + "' not Enabled/Visible after '"
							+ SystemDefaults.waitTime.get() + "' Seconds ", Status.FAIL);
				}
			}

			catch (Exception ex) {
				Report.updateTestLog(Action,
						"Exception while entering '" + Data + "' in '" + ObjectName + "'" + ex.getMessage(),
						Status.FAIL);
				if (ex instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				} else {
					throw new ForcedException(Action, ex.getMessage());
				}
			}
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "Enter the value [<Data>] in the Table Field [<Object>]", input = InputType.YES, condition = InputType.OPTIONAL)
	public void GWSetWebTableValue() {// throws InterruptedException {
		try {
			String strWebElementColXpath = "";
			int iSubIteration = 0;
			int iRowNumber = 0;
			String strRowNumber = "";
			String strData = Data;

			if (Condition != null && !Condition.isEmpty()) {
				strWebElementColXpath = Condition;
			} else {
				strWebElementColXpath = AObject.getObjectProperty(Reference, ObjectName, ObjectProperty.Xpath);
			}

			if (strWebElementColXpath.contains("##ROWNUMBER##")) {
				int strIndex = strWebElementColXpath.indexOf("/table");
				String strXPath = strWebElementColXpath.substring(0, strIndex + 6);

				gRowIndex = Driver.findElements(By.xpath(strXPath)).size();

				// Check the existing row in the Table
				if (gRowIndex == 0) {
					// iSubIteration =
					// Integer.parseInt(userData.getSubIteration());
					iRowNumber = 1; // iSubIteration;
				} else if (gRowIndex > 0) {
					iRowNumber = gRowIndex - 1;
				}
				strRowNumber = Integer.toString(iRowNumber);
				strWebElementColXpath = strWebElementColXpath.replace("##ROWNUMBER##", strRowNumber);
			}

			WebElement element1 = Driver.findElement(By.xpath(strWebElementColXpath));
			if (element1 != null) {
				Thread.sleep(200);
				element1.click();
				executeMethod(Element, "waitForAngularRequestsToFinish");
				WebElement weDriverName = AObject.findElement(element1, ObjectName, Reference);
				if (weDriverName != null) {
					Thread.sleep(200);
					executeMethod(weDriverName, "Set", strData);
					Driver.findElement(By.xpath(".//*[@class='x-img product-logo x-box-item x-img-default']")).click();
					// Report.updateTestLog(Action, "Value [" + Data + "]
					// entered in Element [" + ObjectName + "]", Status.PASS);
				} else {
					Report.updateTestLog(Action, "Element [" + ObjectName + "] not Found", Status.FAIL);
				}
			} else {
				Report.updateTestLog(Action,
						"Element with xPath [" + strWebElementColXpath + "] not Found. Please update the xpath",
						Status.FAIL);
			}
		} catch (Exception ex) {
			Report.updateTestLog(Action, "Exception " + ex.getMessage(), Status.DEBUG);
			if (ex instanceof InterruptedException) {
				// Thread.currentThread().interrupt();
			}
		}
	}

	@Action()
	public void GWStopIterationOnError() {
		try {
			if (Element != null) {
				// Checks the next page Web Element for its presence
				Report.updateTestLog(Action, "Required Element detected Proceed with the Iteration", Status.PASS);
			} else {
				String XPathOfErrorMessage = "(//*[@class='message'])";
				String ActualErrormessage = Driver.findElement(By.xpath(XPathOfErrorMessage)).getText();
				Report.updateTestLog(Action, "Error message appearing in the system -  " + ActualErrormessage + "",
						Status.FAIL);
				SystemDefaults.stopCurrentIteration.set(true);
			}
		} catch (Exception ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Action(object = ObjectType.BROWSER, desc = "Send Keys [ALT+SHIFT+T] to launch Server Tools.", input = InputType.NO)
	public void GWLaunchServerTools() {

		Robot robot;

		String currentBrowser = Browser.fromString(getCurrentBrowserName()).toString();

		switch (currentBrowser) {
		case "IE":
			GWLaunchServerTools_IE();
			break;
		case "Chrome":
			GWLaunchServerTools_Chrome();
			break;
		default:

		}

	}

	public void GWLaunchServerTools_Chrome() {
		try {
			sync();
			Thread.sleep(2000);
			executeMethod("sendKeysToWindow", "Alt+Shift+T");
			Thread.sleep(10000);
			sync();

		} catch (Exception e) {
			Report.updateTestLog(Action, "Error in GWLaunchServerTools_Chrome: " + e.getMessage(), Status.FAIL);
		}
	}

	public void GWLaunchServerTools_IE() {
		Robot robot;
		try {
			int flag = 1;
			GWLaunchServerToolsScreen();

			// To Press ALT+SHIFT+T once again if it didn't navigate to Batch
			// Process Screen
			By TITLE_BATCHPROCESSINFO = By.xpath("//*[@id='BatchProcessInfo:BatchProcessScreen:ttlBar']");
			while (!isElementPresent(TITLE_BATCHPROCESSINFO) && flag < 4) {
				robot = new Robot();
				robot.delay(1000);
				clickOkPopupifExists();
				sync();
				clickLOGO();
				sync();

				robot.delay(2000);

				robot.keyPress(KeyEvent.VK_SHIFT);
				robot.keyPress(KeyEvent.VK_ALT);
				robot.keyPress(KeyEvent.VK_T);
				robot.delay(6000);

				robot.keyRelease(KeyEvent.VK_SHIFT);
				robot.keyRelease(KeyEvent.VK_ALT);
				robot.keyRelease(KeyEvent.VK_T);

				robot.delay(5000);
				System.out.println("Pressed Alt+Shift+T");
				Report.updateTestLog(Action, "Keys [ALT+SHIFT+T] Submitted once again due to sync issue", Status.PASS);
				flag = flag + 1;
			}

			try {
				robot = new Robot();
				robot.keyRelease(KeyEvent.VK_T);
				robot.keyRelease(KeyEvent.VK_SHIFT);
				robot.keyRelease(KeyEvent.VK_ALT);
			} catch (Exception ex) {
				System.out.println("Exception GWLaunchServerTools:" + ex.getMessage());
			}

		} catch (Exception e) {
			Report.updateTestLog(Action, "Error in GWLaunchServerTools_IE: " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(object = ObjectType.BROWSER, desc = "Send Keys [ALT+SHIFT+T] to launch Server Tools.", input = InputType.NO)
	public void GWLaunchServerToolsScreen() {

		Robot robot;
		try {
			// GWclickLogo();
			// Driver.findElement(By.xpath(".//*[@class='x-img product-logo
			// x-box-item x-img-default']")).click();
			robot = new Robot();
			robot.delay(4000);

			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyPress(KeyEvent.VK_T);
			robot.delay(5000);

			robot.keyRelease(KeyEvent.VK_T);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_ALT);

			robot.delay(5000);
			System.out.println("Pressed Alt+Shift+T");

			// Wait till the Batch Process screen loads
			// WebElement ele =
			// Driver.findElement(By.xpath("//*[@id='BatchProcessInfo:BatchProcessScreen:ttlBar']"));
			// new WebDriverWait(Driver,
			// getWaitTime()).until(ExpectedConditions.invisibilityOf(ele));

			Report.updateTestLog(Action, "Keys [ALT+SHIFT+T] Submitted Successfully", Status.PASS);
		} catch (Exception e) {
			Report.updateTestLog(Action, "Error in Sending Keys: " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "Send Keys [<Data>] to IE Window/Element.", input = InputType.YES)
	public void sendKeysToIEWindow() {

		Robot robot;
		try {
			Driver.findElement(By.xpath(".//*[@class='x-img product-logo x-box-item x-img-default']")).click();
			robot = new Robot();
			// robot.delay(1000);
			String[] values = Data.toLowerCase().split("\\+");
			// Key Press Event for keys passed in input
			for (int i = 0; i < values.length; i++) {
				robot.keyPress(getKeyCode(values[i]));
			}
			robot.delay(500);
			// Key release Event for keys passed in input (reverse order so that
			// last pressed key will be released first)
			for (int i = values.length - 1; i >= 0; i--) {
				robot.keyRelease(getKeyCode(values[i]));
			}

			Report.updateTestLog(Action, "Keys " + Data + " Submitted Successfully", Status.PASS);
		} catch (Exception e) {
			Report.updateTestLog(Action, "Error in Sending Keys - " + e.getMessage(), Status.FAIL);
		}
	}

	int getKeyCode(String data) {
		switch (data) {
		case "tab":
			return KeyEvent.VK_TAB;
		case "enter":
			return KeyEvent.VK_ENTER;
		case "shift":
			return KeyEvent.VK_SHIFT;
		case "ctrl":
			return KeyEvent.VK_CONTROL;
		case "alt":
			return KeyEvent.VK_ALT;
		case "esc":
			return KeyEvent.VK_ESCAPE;
		case "delete":
			return KeyEvent.VK_DELETE;
		case "backspace":
			return KeyEvent.VK_BACK_SPACE;
		case "home":
			return KeyEvent.VK_HOME;
		case "a":
			return KeyEvent.VK_A;
		case "c":
			return KeyEvent.VK_C;
		case "k":
			return KeyEvent.VK_K;
		case "m":
			return KeyEvent.VK_M;
		case "p":
			return KeyEvent.VK_P;
		case "t":
			return KeyEvent.VK_T;
		default:
			return 0;
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "Retrieve and Store value in Table [<Object>] matching [<Data>]", input = InputType.YES, condition = InputType.YES)
	public void GWGetWebTableValue() {// throws InterruptedException {
		String expValue = "";
		String strWebElementColXpath = "";
		try {
			Driver.findElement(By.xpath(".//*[@class='x-img product-logo x-box-item x-img-default']")).click();
			strWebElementColXpath = AObject.getObjectProperty(Reference, ObjectName, ObjectProperty.Xpath);

			// Check if input has additional string on expected column
			if (Data.contains(";")) {
				String[] strData = Data.split(";");
				strWebElementColXpath = strWebElementColXpath.replace("##randomString##", strData[0].trim());
				strWebElementColXpath = strWebElementColXpath.replace("##randomColumn##", strData[1].trim());
			} else {
				strWebElementColXpath = strWebElementColXpath.replace("##randomString##", Data.trim());
			}

			// Capture text against the xPath given
			expValue = Driver.findElement(By.xpath(strWebElementColXpath)).getText();

			// Add captured text to variable Name if one is given
			if (Condition.startsWith("%") && Condition.endsWith("%")) {
				addVar(Condition, expValue);
				Report.updateTestLog(Action, "Text : " + expValue + " stored in variable " + Condition, Status.PASS);
			}
			// Add captured text to Data Sheet otherwise
			else if (Condition.matches(".*:.*")) {
				userData.putData(Condition.split(":")[0], Condition.split(":")[1], expValue);
				Report.updateTestLog(Action, "Text : " + expValue + " stored in Data Sheet " + Condition.split(":")[0],
						Status.PASS);
			} else {
				Report.updateTestLog(Action,
						"Invalid format in Condition Column. Captured Text " + expValue + " is not stored",
						Status.FAIL);
			}

		} catch (Exception ex) {
			Report.updateTestLog(Action, "Error capturing/storing text " + ex.getMessage(), Status.DEBUG);
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "Retrieve value in Table [<Object>] matching [<Data>] and Validate against [<Condition>]", input = InputType.YES, condition = InputType.OPTIONAL)
	public void GWGetAndCheckTableValue() {// throws InterruptedException {
		try {
			Driver.findElement(By.xpath(".//*[@class='x-img product-logo x-box-item x-img-default']")).click();
			String strWebElementColXpath = AObject.getObjectProperty(Reference, ObjectName, ObjectProperty.Xpath);
			String expValue = "";
			String input = "";
			// Check if input has additional string on expected column
			if (Condition.isEmpty()) {
				if (Data.contains(":")) {
					String sheetName = Data.split(":", 2)[0];
					String columnName = Data.split(":", 2)[1];
					input = userData.getData(sheetName, columnName);
				} else if (Data.contains("%")) {
					input = getVar(Data);
				} else {
					input = Data;
				}
				expValue = Driver.findElement(By.xpath(strWebElementColXpath)).getText();
			} else {
				if (Data.contains(";")) {

					String[] strData = Data.split(";");
					strWebElementColXpath = strWebElementColXpath.replace("##randomString##", strData[0].trim());
					strWebElementColXpath = strWebElementColXpath.replace("##randomColumn##", strData[1].trim());
				} else if (Data.contains("%")) {
					Data = getVar(Data);
					strWebElementColXpath = strWebElementColXpath.replace("##randomString##", Data.trim());
				} else {
					strWebElementColXpath = strWebElementColXpath.replace("##randomString##", Data.trim());
				}

				expValue = Driver.findElement(By.xpath(strWebElementColXpath)).getText();

				// Compare text identified for the xPath against what is
				// provided in Condition
				if (Condition.contains(":")) {
					String sheetName = Condition.split(":", 2)[0];
					String columnName = Condition.split(":", 2)[1];
					input = userData.getData(sheetName, columnName);
				} else if (Condition.contains("%")) {
					input = getVar(Condition);
				} else {
					input = Condition;
				}
			}

			if (expValue.equalsIgnoreCase(input) || expValue.contains(input)) {
				Report.updateTestLog(Action,
						"Actual Text : '" + expValue + "' matches or contains with Expected : '" + input + "'",
						Status.PASS);
			} else {
				Report.updateTestLog(Action,
						"Actual Text : '" + expValue + "' Does Not matches or contains with Expected : '" + input + "'",
						Status.FAIL);
			}

		} catch (Exception ex) {
			Report.updateTestLog(Action, "Error retrieving text or validate" + ex.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "Store text provided in [<Data>] into datasheet:columname [<Condition>]", input = InputType.YES, condition = InputType.YES)
	public void GWUpdateTextInDataSheet() {
		try {
			if (Data.contains("%")) {
				Data = getVar(Data);
			}

			if (Condition.contains(":")) {
				String sheetName = Condition.split(":", 2)[0];
				String columnName = Condition.split(":", 2)[1];
				userData.putData(sheetName, columnName, Data);
				Report.updateTestLog(Action, "Provided text [" + Data + "] is stored in Data Sheet" + Condition,
						Status.DONE);
			} else {
				Report.updateTestLog(Action,
						"Given Data Sheet [" + Condition + "] format is invalid. It should be [SheetName:ColumnName]",
						Status.DEBUG);
			}
		} catch (Exception ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.OFF, ex.getMessage(), ex);
			Report.updateTestLog(Action, "Error Storing text in datasheet " + ex.getMessage(), Status.DEBUG);
		}
	}

	@Action(desc = "Sum amounts provided in [<Data>] and [<Condition>] and enter into Object [ObjectName] or variable %AddedAmt%", input = InputType.YES, condition = InputType.YES)
	public void GWSumAndEnterAmount() {
		String amount1 = "0";
		String amount = "0";
		double sumAmount = 0.0;
		String sumAmt = "";
		DecimalFormat f = new DecimalFormat("#.00");

		try {
			if (Data.contains("$")) {
				Data = Data.replace("$", "");
			}

			if (Data.contains(",")) {
				Data = Data.replace(",", "");
			}

			amount = Data;
			if (Condition.contains(":")) {
				String sheetName = Condition.split(":", 2)[0];
				String columnName = Condition.split(":", 2)[1];
				amount1 = userData.getData(sheetName, columnName);
				if (amount1.contains("$")) {
					amount1 = amount1.replace("$", "");
				}
			} else if (Condition.contains("%")) {
				amount1 = getVar(Condition);

				if (amount1.contains("$")) {
					amount1 = amount1.replace("$", "");
				}
				if (amount1.contains(",")) {
					amount1 = amount1.replace(",", "");
				}
			} else {
				amount1 = Condition;
			}

			if (amount.equals("-")) {
				amount = "0";
			}
			if (amount1.equals("-")) {
				amount1 = "0";
			}

			if (amount.contains("(")) {
				amount = amount.replace("(", "-");
			}
			if (amount.contains(")")) {
				amount = amount.replace(")", "");
			}

			if (amount1.contains("(")) {
				amount1 = amount1.replace("(", "-");
			}
			if (amount1.contains(")")) {
				amount1 = amount1.replace(")", "");
			}

			sumAmount = Double.valueOf(amount) + Double.valueOf(amount1);
			sumAmt = f.format(sumAmount);
			Data = sumAmt; // Double.toString(sumAmt);

			if (ObjectName.equalsIgnoreCase("Browser")) {
				addVar("%AddedAmt%", Data);
				Report.updateTestLog(Action, "Amount [" + Data + "] Stored into variable %AddedAmt%", Status.DONE);
			} else {
				Element = AObject.findElement(ObjectName, Reference);
				new WaitFor(getCommander()).waitForElementToBePresent();

				if (Element.isEnabled()) {
					Element.clear();
					executeMethod(Element, "waitForAngularRequestsToFinish");
					Element.sendKeys(Data);
					Thread.sleep(500);
					Report.updateTestLog(Action, "Amount [" + Data + "] Entered into Object '" + ObjectName + "'",
							Status.DONE);
				}
			}

		} catch (Exception ex) {
			Report.updateTestLog(Action,
					"Error while summing up given values " + amount1 + "," + amount + ex.getMessage(), Status.DEBUG);
			Logger.getLogger(this.getClass().getName()).log(Level.OFF, ex.getMessage(), ex);
		}
	}

	@Action(desc = "Subtract amounts provided in [<Data>] and [<Condition>] and enter into Object [ObjectName] or variable %SubAmt%", input = InputType.YES, condition = InputType.YES)
	public void GWSubtractAndEnterAmount() {
		String amount1 = "0";
		String amount = "0";
		double subAmount = 0.0;
		String subAmt = "";
		DecimalFormat f = new DecimalFormat("#.00");

		try {
			if (Data.contains("$")) {
				Data = Data.replace("$", "");
			}

			if (Data.contains(",")) {
				Data = Data.replace(",", "");
			}

			amount = Data;

			if (Condition.contains(":")) {
				String sheetName = Condition.split(":", 2)[0];
				String columnName = Condition.split(":", 2)[1];
				amount1 = userData.getData(sheetName, columnName);
				if (amount1.contains("$")) {
					amount1 = amount1.replace("$", "");
				}
			} else if (Condition.contains("%")) {
				amount1 = getVar(Condition);

				if (amount1.contains("$")) {
					amount1 = amount1.replace("$", "");
				}
				if (amount1.contains(",")) {
					amount1 = amount1.replace(",", "");
				}
			} else {
				amount1 = Condition;
			}

			if (amount.equals("-")) {
				amount = "0";
			}
			if (amount1.equals("-")) {
				amount1 = "0";
			}

			if (amount.contains("(")) {
				amount = amount.replace("(", "-");
			}
			if (amount.contains(")")) {
				amount = amount.replace(")", "");
			}

			if (amount1.contains("(")) {
				amount1 = amount1.replace("(", "-");
			}
			if (amount1.contains(")")) {
				amount1 = amount1.replace(")", "");
			}

			subAmount = Double.valueOf(amount) - Double.valueOf(amount1);
			subAmt = f.format(subAmount);
			Data = subAmt; // Double.toString(sumAmt);

			if (ObjectName.equalsIgnoreCase("Browser")) {
				addVar("%SubAmt%", Data);
				Report.updateTestLog(Action, "Amount [" + Data + "] Stored into variable %SubAmt%", Status.DONE);
			} else {
				Element = AObject.findElement(ObjectName, Reference);
				new WaitFor(getCommander()).waitForElementToBePresent();

				if (Element.isEnabled()) {
					Element.clear();
					executeMethod(Element, "waitForAngularRequestsToFinish");
					Element.sendKeys(Data);
					Thread.sleep(500);
					Report.updateTestLog(Action, "Amount [" + Data + "] Entered into Object '" + ObjectName + "'",
							Status.DONE);
				}
			}

		} catch (Exception ex) {
			Report.updateTestLog(Action,
					"Error while summing up given values " + amount1 + "," + amount + ex.getMessage(), Status.DEBUG);
			Logger.getLogger(this.getClass().getName()).log(Level.OFF, ex.getMessage(), ex);
		}
	}

	@Action(desc = "Sum amounts provided in [<Data>] and [<Condition>] and variable %AddedAmt%", input = InputType.YES, condition = InputType.YES)
	public void GWSumFields() {
		String amount1 = "0";
		String amount = "0";
		Integer sumAmount = 0;
		try {
			if (Data.contains("$")) {
				Data = Data.replace("$", "");
			}
			amount = Data;
			if (Condition.contains(":")) {
				String sheetName = Condition.split(":", 2)[0];
				String columnName = Condition.split(":", 2)[1];
				amount1 = userData.getData(sheetName, columnName);
				if (amount1.contains("$")) {
					amount1 = amount1.replace("$", "");
				}
			} else if (Condition.contains("%")) {
				amount1 = getVar(Condition);

				if (amount1.contains("$")) {
					amount1 = amount1.replace("$", "");
				}
			} else {
				amount1 = Condition;
			}

			sumAmount = Integer.valueOf(amount) + Integer.valueOf(amount1);
			Data = new Integer(sumAmount).toString();

			if (ObjectName.equalsIgnoreCase("Browser")) {
				addVar("%AddedAmt%", Data);
				Report.updateTestLog(Action, "Amount [" + Data + "] Stored into variable %AddedAmt%", Status.DONE);
			} else {
				Element = AObject.findElement(ObjectName, Reference);
				new WaitFor(getCommander()).waitForElementToBePresent();
				Thread.sleep(500);

				if (Element.isEnabled()) {
					Element.clear();
					Thread.sleep(100);
					Element.sendKeys(Data);
					Thread.sleep(100);
					Report.updateTestLog(Action, "Amount [" + Data + "] Entered into Object '" + ObjectName + "'",
							Status.DONE);
				}
			}

		} catch (Exception ex) {
			Report.updateTestLog(Action,
					"Error while summing up given values " + amount1 + "," + amount + ex.getMessage(), Status.DEBUG);
			Logger.getLogger(this.getClass().getName()).log(Level.OFF, ex.getMessage(), ex);
		}
	}

	@Action(desc = "Divide amounts provided in [<Data>] and [<Condition>] and variable %DividedAmt%", input = InputType.YES, condition = InputType.YES)
	public void GWDivideValues() {
		String amount1 = "0";
		String amount = "0";
		double divAmount = 0.0;
		String divAmt = "0";
		DecimalFormat f = new DecimalFormat("#.00");

		try {
			if (Data.contains("$")) {
				Data = Data.replace("$", "");
			}

			if (Data.contains(",")) {
				Data = Data.replace(",", "");
			}

			if (Data.contains("(")) {
				Data = Data.replace("(", "-");
			}

			if (Data.contains(")")) {
				Data = Data.replace("(", "");
			}

			amount = Data;

			if (Condition.contains(":")) {
				String sheetName = Condition.split(":", 2)[0];
				String columnName = Condition.split(":", 2)[1];
				amount1 = userData.getData(sheetName, columnName);
			} else if (Condition.contains("%")) {
				amount1 = getVar(Condition);
			} else {
				amount1 = Condition;
			}

			if (amount1.contains("$")) {
				amount1 = amount1.replace("$", "");
			}

			if (amount1.contains(",")) {
				amount1 = amount1.replace(",", "");
			}

			if (amount1.contains("(")) {
				amount1 = amount1.replace("(", "-");
			}

			if (amount1.contains(")")) {
				amount1 = amount1.replace(")", "");
			}

			divAmount = Double.valueOf(amount) / Double.valueOf(amount1);
			divAmt = f.format(divAmount);
			Data = divAmt;
			// Data=new Integer(divAmount).toString();

			if (ObjectName.equalsIgnoreCase("Browser")) {
				addVar("%DividedAmt%", Data);
				Report.updateTestLog(Action, "Amount [" + Data + "] Stored into variable %DividedAmt%", Status.DONE);
			} else {
				Element = AObject.findElement(ObjectName, Reference);
				new WaitFor(getCommander()).waitForElementToBePresent();
				Thread.sleep(500);

				if (Element.isEnabled()) {
					Element.clear();
					Thread.sleep(100);
					Element.sendKeys(Data);
					Thread.sleep(100);
					Report.updateTestLog(Action, "Amount [" + Data + "] Entered into Object '" + ObjectName + "'",
							Status.DONE);
				}
			}

		} catch (Exception ex) {
			Report.updateTestLog(Action,
					"Error while dividing given values " + amount + "," + amount1 + ": " + ex.getMessage(),
					Status.FAIL);
			Logger.getLogger(this.getClass().getName()).log(Level.OFF, ex.getMessage(), ex);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// To DO Ismail
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Action(desc = "To click on Logo Object")
	public void GWclickLogo() {

		clickLOGO();
	}

	@Action(desc = "To click on OK Button in Popup window")
	public void GWclickOkPopupifExists() {
		clickOkPopupifExists();
	}

	@Action(object = ObjectType.SELENIUM, desc = "To click on object")
	public void GWclick() {
		try {
			if (isElementPresent(Element)) {
				clickByJS(Element);
				Report.updateTestLog(Action, "Clicked on object " + Element.getText(), Status.PASS);
			} else
				Report.updateTestLog(Action, "Element not Present " + Element.getText(), Status.FAIL);
		}

		catch (StaleElementReferenceException stale) {
			try {
				WebElement Element = AObject.findElement(ObjectName, Reference);
				if (isElementPresent(Element)) {
					Element.click();
					Report.updateTestLog(Action, "Clicked on object " + Element.getText(), Status.PASS);
				} else {
					stale.printStackTrace();
					Report.updateTestLog(Action, "Exception in method. " + stale.getMessage(), Status.FAIL);
				}
			} catch (StaleElementReferenceException e) {
				Report.updateTestLog(Action, "Exception handled in GWclick." + e.getMessage(), Status.DONE);
			}

		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
		}

	}

	@Action(object = ObjectType.SELENIUM, desc = "To click on object if exists")
	public void GWclickifExist() {

		if (isElementPresent(Element)) {
			clickByJS(Element);
			Report.updateTestLog(Action, "Clicked on object " + Element.getText(), Status.PASS);
		}

	}

	@Action(object = ObjectType.SELENIUM, desc = "To click on object ignoring popup")
	public void GWclickIgnorePopup() {
		try {
			if (isElementPresent(Element)) {
				clickByJS(Element);
				Report.updateTestLog(Action, "Clicked on object " + Element.getText(), Status.PASS);
				sync();
				// Click 'ok' if popup exists and click on the given object
				if (isExists(getButton("OK"))) {
					clickOkPopupifExists();
					sync();

					Thread.sleep(3000);

					clickIfExists(Element);
					Report.updateTestLog(Action, "Clicked on object " + Element.getText() + " after closing pop-up",
							Status.PASS);
				}

			} else
				Report.updateTestLog(Action, "Element not Present " + Element.getText(), Status.FAIL);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.DONE);

			clickIfExists(Element);
			Report.updateTestLog(Action, "Clicked on element " + Element.getText(), Status.PASS);
			sync();

		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "To ensure that the text <Data> is not present in <Element>", input = InputType.YES)
	public void GWassertElementTextNotContains() {
		try {
			String eleText = "";
			eleText = Element.getText();
			if (eleText.equals("")) {
				eleText = Element.getAttribute("value");
			}

			if (eleText.contains(Data)) {
				Report.updateTestLog(Action, "Element text '" + eleText + "' contains '" + Data + "'", Status.FAIL);
			} else {
				Report.updateTestLog(Action, "Element text '" + eleText + "' doesn't contains '" + Data + "'",
						Status.PASS);
			}
		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "To assert whether the text from an <Element> is Positive ")
	public void GWassertIfPositiveNumber() {
		try {
			String eleText = "";
			double eleNumber;
			eleText = Element.getText();
			if (eleText.equals("")) {
				eleText = Element.getAttribute("value");
			}

			if (!eleText.equals("")) {
				eleNumber = Double.parseDouble(eleText);
				if (eleNumber >= 0)
					Report.updateTestLog(Action, ObjectName + " value '" + eleText + "' is a Positive Number",
							Status.PASS);
				else
					Report.updateTestLog(Action, ObjectName + " value '" + eleText + "' is not a Positive Number",
							Status.FAIL);
			} else {
				Report.updateTestLog(Action, "Element value is blank", Status.FAIL);
			}
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "To assert whether the text from an <Element> is Negative ")
	public void GWassertIfNegativeNumber() {
		try {
			String eleText = "";
			double eleNumber;
			eleText = Element.getText();
			if (eleText.equals("")) {
				eleText = Element.getAttribute("value");
			}

			if (!eleText.equals("")) {
				eleNumber = Double.parseDouble(eleText);
				if (eleNumber < 0)
					Report.updateTestLog(Action, "Element value '" + eleText + "' is a Negative Number", Status.PASS);
				else
					Report.updateTestLog(Action, "Element value '" + eleText + "' is not a Negative Number",
							Status.FAIL);
			} else {
				Report.updateTestLog(Action, "Element value is blank", Status.FAIL);
			}
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception. " + e.getMessage(), Status.FAIL);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// To DO Ismail
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*******************************************************************************************************
	 * Object Level Methods - CITS
	 * 
	 * @author Ismail
	 ********************************************************************************************************/
	@Action(desc = "To Handle Sync")
	public void GWsync() {
		sync();

	}

	/**
	 * To Click OK button in popup if Exists
	 */

	@Action(desc = "To click OK button if popup exists")
	public void GW_clickOkAlertifExists() {
		try {
			acceptAlertifExists();
			Thread.sleep(1000);
			Report.updateTestLog(Action, "Clicked OK button in the Alert box", Status.PASS);
		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*******************************************************************************************************
	 * Object Level Methods
	 * 
	 * @author Ismail
	 ********************************************************************************************************/

	/**
	 * To Handle browser Synchronization. It will allow the script to wait till
	 * page load is complete
	 */
	public void sync() {
		try {
			sleep(SYNC_WAIT_MILLISECONDS);
			WebElement loading = find(LOADING_DIV);
			new WebDriverWait(Driver, getWaitTime()).until(ExpectedConditions.invisibilityOf(loading));
		} catch (Exception ex) {

		}
	}

	/**
	 * Retruns the wait time
	 * 
	 * @return
	 */
	public static int getWaitTime() {
		return waitTime;
	}

	/**
	 * Allow the script to wait for the given milliseconds
	 * 
	 * @param ms
	 */
	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}

	}

	/**
	 * Find element by given locator
	 * 
	 * @param locator
	 *            locator to use
	 * @return the element
	 */
	public WebElement find(By locator) {
		return Driver.findElement(locator);
	}

	/**
	 * get random number of given length
	 * 
	 * @param noDigits
	 *            length of digit
	 * @return random number of given length
	 */
	public String getRandomNumber(int noDigits) {
		int ll = (int) java.lang.Math.pow(10, noDigits - 1);
		int ul = (int) java.lang.Math.pow(10, noDigits) - ll;
		return "" + ((int) (ll + new java.util.Random().nextFloat() * ul));
	}

	/**
	 * Enter values on given input element
	 * 
	 * @param element
	 *            target input element
	 * @param keysToSend
	 *            values to type
	 */
	public void type(WebElement element, String keysToSend) {
		sync();
		element.clear();
		sync();
		element.sendKeys(keysToSend);
		element.sendKeys(Keys.TAB);
	}

	/**
	 * Enter values on given input element
	 * 
	 * @param element
	 *            target input element
	 * @param keysToSend
	 *            values to type
	 */
	public void type(By locator, String keysToSend) {
		this.waitforElementPresent(locator);
		type(find(locator), keysToSend);
		log("set", keysToSend + " set in " + getElementTextifExists(locator), "PASS");
	}

	/**
	 * To wait for the element to be present
	 * 
	 * @param by
	 *            locator
	 */
	public void waitforElementPresent(By by) {
		sync();
		waitforElementPresent(by, DEFAULT_WAIT_TIME);
	}

	/**
	 * To wait for the element to be present till the given wait time Parameters
	 * 
	 * @param by
	 *            locator
	 * @param waitTime
	 *            wait time in seconds
	 */
	public void waitforElementPresent(By by, int waitTimeSeconds) {
		try {
			sync();
			WebDriverWait wait = new WebDriverWait(Driver, waitTimeSeconds);
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
		} catch (Exception ex) {
			throw new RuntimeException("Error in Waiting for Element" + by, ex);
		}
	}

	/**
	 * To wait for the element to be present till the given wait time Parameters
	 * 
	 * @param by
	 *            locator
	 * @param waitTime
	 *            wait time in seconds
	 */
	public void waitforElementPresent(WebElement ele, int waitTimeSeconds) {
		try {
			sync();
			WebDriverWait wait = new WebDriverWait(Driver, waitTimeSeconds);
			wait.until(ExpectedConditions.visibilityOf(ele));
		} catch (Exception ex) {
			throw new RuntimeException("Error in Waiting for Element" + ele.getText(), ex);
		}
	}

	/**
	 * To check whether the element is present
	 * 
	 * @param by
	 *            locator
	 */
	public boolean isElementPresent(By by) {
		try {
			waitforElementPresent(by);
			WebElement Element = Driver.findElement(by);
			return Element != null;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * To check whether the element is present
	 * 
	 * @param by
	 *            locator
	 */
	public boolean isElementPresent(WebElement ele) {
		try {
			// waitforElementPresent(by);
			return ele != null;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * To check whether the element is present if exists
	 * 
	 * @param by
	 *            locator
	 */
	public boolean isExists(By by) {
		try {
			WebElement Element = Driver.findElement(by);
			return Element != null;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * To check whether the element is Enabled
	 * 
	 * @param by
	 *            locator
	 */
	public boolean isElementEnabled(By by) {
		try {
			if (isElementPresent(by)) {
				WebElement Element = Driver.findElement(by);
				return Element.isEnabled();
			} else
				return false;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * To check whether the element is Displayed
	 * 
	 * @param by
	 *            locator
	 */
	public boolean isElementDisplayed(By by) {
		try {
			if (isElementPresent(by)) {
				WebElement Element = Driver.findElement(by);
				return Element.isDisplayed();
			} else
				return false;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Returns the text/value of the Element if exists
	 * 
	 * @param by
	 *            locator
	 * @return element text
	 */
	public String getElementTextifExists(By by) {

		try {
			WebElement element = Driver.findElement(by);
			if (element.getTagName().equalsIgnoreCase("input")) {
				return getTextForInput(by, element);
			} else if (element.getTagName().equalsIgnoreCase("a")) {
				return getTextForAnchor(by, element);
			} else if (element.getTagName().equalsIgnoreCase("select")) {
				return getTextOrLocator(by, new Select(element).getFirstSelectedOption());
			} else {
				return getTextOrLocator(by, element);
			}
		} catch (Exception e) {
			return by.toString();
		}

	}

	/**
	 * Get element text if present or get locator
	 * 
	 * @param locator
	 *            element locator
	 * @param element
	 *            web element
	 * @return element text if present or locator
	 */
	private String getTextOrLocator(By locator, WebElement element) {
		try {
			String text = element.getText();
			if (!"".equals(text))
				return text;
		} catch (Exception e) {
			return locator.toString();
		}
		return locator.toString();
	}

	/**
	 * Get Inner text for the anchor element
	 * 
	 * @param locator
	 *            anchor locator
	 * @param anchor
	 *            anchor element
	 * @return text for the anchor element
	 */
	private String getTextForAnchor(By locator, WebElement anchor) {
		try {
			return Objects
					.toString(((JavascriptExecutor) Driver).executeScript("return arguments[0].innerText", anchor),
							locator.toString())
					.trim();
		} catch (Exception e) {
			return locator.toString();
		}
	}

	/**
	 * Get label text for the input
	 * 
	 * @param locator
	 *            input locator
	 * @param input
	 *            input element
	 * @return label text for the input
	 */
	private String getTextForInput(By locator, WebElement input) {
		try {
			return getTextOrLocator(locator,
					By.cssSelector(String.format("label[for='%s']", input.getAttribute("id"))).findElement(Driver));
		} catch (Exception e) {
			return locator.toString();
		}
	}

	/**
	 * Click on element
	 * 
	 * @param by
	 *            locator
	 */
	public void click(By by) {
		try {
			if (isElementPresent(by)) {
				new WebDriverWait(Driver, 5).ignoring(StaleElementReferenceException.class, WebDriverException.class)
				.until(driver -> {
					find(by).click();
					return true;
				});
				log("click", "Clicked on " + getElementTextifExists(by), "PASS");
			} else {
				throw new RuntimeException("Element not present " + by);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error in click " + by, ex);
		}

	}

	/**
	 * Click on element if exists
	 * 
	 * @param by
	 *            locator
	 */
	public void clickIfExists(By by) {
		try {
			if (isExists(by)) {
				sync();
				find(by).click();
				log("clickIfExists", "Clicked on " + getElementTextifExists(by), "PASS");
			}
		} catch (Exception ex) {
			// throw new RuntimeException("Error in clickIfExists", ex);
		}

	}

	/**
	 * Click on element if exists
	 * 
	 * @param by
	 *            locator
	 */
	public void clickIfExists(WebElement ele) {
		try {
			if (isElementPresent(ele)) {
				sync();
				ele.click();
				log("clickIfExists", "Clicked on " + ele.getText(), "PASS");
			}
		} catch (Exception ex) {
			// throw new RuntimeException("Error in clickIfExists", ex);
		}

	}

	/**
	 * Click on element using Javascript
	 * 
	 * @param by
	 *            locator
	 */
	public void clickByJS(By by) {
		try {
			if (isElementPresent(by)) {
				WebElement Element = Driver.findElement(by);
				((JavascriptExecutor) Driver).executeScript("arguments[0].click();", Element);
				log("clickByJS", "Clicked nu JS on " + by, "PASS");
			} else {
				throw new RuntimeException("Element not present");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error in clickByJS " + by, ex);
		}
	}

	/**
	 * Click on element using Javascript
	 * 
	 * @param by
	 *            locator
	 */
	public void clickByJS(WebElement ele) {
		try {
			if (isElementPresent(ele)) {
				((JavascriptExecutor) Driver).executeScript("arguments[0].click();", ele);
				// log("clickByJS", "Clicked by JS on " + ele.getText(),
				// "PASS");
			} else {
				throw new RuntimeException("Element not present");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error in clickByJS " + ele.getText(), ex);
		}
	}

	/**
	 * Double click on element
	 * 
	 * @param by
	 *            locator
	 */

	public void doubleClick(By by) {
		try {
			if (isElementPresent(by)) {
				Actions action = new Actions(Driver);
				action.doubleClick(Driver.findElement(by)).click().perform();
			} else {
				throw new RuntimeException("Element not present");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error in doubleClick " + by, ex);
		}
	}

	/**
	 * Double click on element by java script
	 * 
	 * @param by
	 *            locator
	 */
	public void doubleClickByJS(By by) {
		try {
			if (isElementPresent(by)) {
				WebElement element = Driver.findElement(by);
				((JavascriptExecutor) Driver).executeScript("arguments[0].dblclick();", element);
			} else {
				throw new RuntimeException("Element not present");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error in doubleClickByJS " + by, ex);
		}
	}

	/**
	 * To set value in a edit box
	 * 
	 * @param by
	 *            locator
	 * @param data
	 *            input
	 */
	public void set(By by, String data) {
		try {
			if (!Objects.toString(data, "").isEmpty()) {
				if (data.trim().equalsIgnoreCase("BLANK"))
					data = "";
				sync();
				if (isElementPresent(by)) {
					if (isElementEnabled(by)) {
						String type = find(by).getTagName();
						if ("input".equalsIgnoreCase(type) || "textarea".equalsIgnoreCase(type))
							clickClearSet(by, data);
						else if ("select".equalsIgnoreCase(type))
							new Select(find(by)).selectByVisibleText(data);
						else
							throw new RuntimeException("Invalid locator " + by);
						log("set", data + " set in " + getElementTextifExists(by), "PASS");
					} else
						throw new RuntimeException("Element not Enabled " + by);
				} else
					throw new RuntimeException("Element not Present " + by);
			}

		} catch (Exception ex) {
			throw new RuntimeException("Error in Setting value for Element " + by, ex);
		}
	}

	/**
	 * To set edit box value in a table
	 * 
	 * @param by
	 *            locator for table cell
	 * @param value
	 *            data for the input
	 */
	public void setCellValue(By by, By byFocusInput, String value) {
		try {

			if (!Objects.toString(value, "").isEmpty()) {
				if (value.trim().equalsIgnoreCase("BLANK"))
					value = "";
				sync();
				if (isElementPresent(by)) {
					if (isElementEnabled(by)) {
						click(by);
						waitforElementPresent(byFocusInput);
						clickClearSet(byFocusInput, value);
						sleep(500);
						escape(byFocusInput);
						log("setCellValue", value, "PASS");
					} else {
						throw new RuntimeException("Element not Enabled" + by);
					}
				} else {
					throw new RuntimeException("Element not Present" + by);
				}
			}

		} catch (Exception ex) {
			throw new RuntimeException("Error in setCellValue " + by, ex);
		}
	}

	/**
	 * escape on focused input(mostly table cell focused input)
	 * 
	 * @param byFocusInput
	 *            locator
	 */
	public void escape(By byFocusInput) {
		try {
			Optional.ofNullable(findIf(byFocusInput)).ifPresent(it -> it.sendKeys(Keys.ESCAPE));
		} catch (Exception ex) {
			log("escape", "Error while escaping " + byFocusInput + " : " + ex, "Done");
		}
	}

	/**
	 * Find WebElement if present, return null if not
	 * 
	 * @param locator
	 *            element locator
	 * @return WebElement if present, null if not
	 */
	private WebElement findIf(By locator) {
		try {
			return find(locator);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * click on input and update
	 * 
	 * @param input
	 *            locator
	 * @param value
	 *            data to enter
	 */
	private void clickClearSet(By input, String value) {
		new WebDriverWait(Driver, 5).ignoring(StaleElementReferenceException.class, WebDriverException.class)
		.until(driver -> {
			driver.findElement(input).click();
			return true;
		});
		clearSet(input, value);
	}

	/**
	 * click on input and update
	 * 
	 * @param input
	 *            locator
	 * @param value
	 *            data to enter
	 */
	private void clickClearSet(WebElement ele, String value) {
		new WebDriverWait(Driver, 5).ignoring(StaleElementReferenceException.class, WebDriverException.class)
		.until(driver -> {
			ele.click();
			return true;
		});
		clearSet(ele, value);
	}

	/**
	 * clear input and set given value
	 * 
	 * @param input
	 *            locator
	 * @param value
	 *            data to enter
	 */
	public void clearSet(WebElement ele, String value) {
		new WebDriverWait(Driver, 5).ignoring(StaleElementReferenceException.class).until(driver -> {
			ele.clear();
			return true;
		});
		sleep(500);
		ele.sendKeys(value);
		// wait for search to render
		sleep(500);
		// ele.sendKeys(Keys.TAB);
		clickLogo();
	}

	/**
	 * click on app. logo
	 */
	public void clickLogo() {
		try {
			sync();
			WebElement ele = Driver.findElement(IMG_LOGO);
			clickIfExists(ele);
			sync();
		} catch (Exception ex) {

		}
	}

	/**
	 * clear input and set given value
	 * 
	 * @param input
	 *            locator
	 * @param value
	 *            data to enter
	 */
	public void clearSet(By input, String value) {
		new WebDriverWait(Driver, 5).ignoring(StaleElementReferenceException.class).until(driver -> {
			driver.findElement(input).clear();
			return true;
		});
		sleep(500);
		find(input).sendKeys(value);
		// wait for search to render
		sleep(200);
		find(input).sendKeys(Keys.TAB);
	}

	/**
	 * click on app. logo
	 */
	public void clickLOGO() {
		try {
			clickIfExists(IMG_LOGO);
		} catch (Exception ex) {

		}
	}

	/**
	 * Move focus to given element
	 * 
	 * @param locator
	 */
	public void moveTo(By locator) {
		waitforElementPresent(locator);
		new Actions(Driver).moveToElement(find(locator)).build().perform();
	}

	/**
	 * Accept Alert
	 */

	public void acceptAlert() {
		waitforAlertPresent();
		Driver.switchTo().alert().accept();
		Driver.switchTo().defaultContent();
	}

	/**
	 * Accept Alert
	 */

	public void acceptAlertifExists() {
		try {
			waitforAlertPresent();
			Driver.switchTo().alert().accept();
			Driver.switchTo().defaultContent();
		} catch (Exception e) {
			log("acceptAlertifExists", "Alert not exists", "Done");
		}
	}

	/**
	 * Dismiss Alert
	 */

	public void dismissAlert() {
		waitforAlertPresent();
		Driver.switchTo().alert().dismiss();
		Driver.switchTo().defaultContent();
	}

	/**
	 * Dismiss Alert
	 */

	public void waitforAlertPresent() {
		new WebDriverWait(Driver, waitTime).until(ExpectedConditions.alertIsPresent());
	}

	/**
	 * check if alert is present
	 * 
	 * @return
	 */
	@Override
	public boolean isAlertPresent() {
		try {
			WebDriverWait wait = new WebDriverWait(Driver, 3);
			wait.until(ExpectedConditions.alertIsPresent());
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * wait for element to be invisible
	 * 
	 * @param locator
	 */
	public void waitforElementTobeInvisible(By locator) {
		new WebDriverWait(Driver, waitTime).until(ExpectedConditions.invisibilityOfElementLocated(locator));
	}

	protected Object executeJS(String script, Object... args) {
		return ((JavascriptExecutor) Driver).executeScript(script, args);
	}

	/**
	 * print log message for an action
	 * 
	 * @param action
	 *            called function
	 * @param desc
	 *            log desc
	 * @param status
	 *            step status
	 */
	public void log(String action, String desc, String status) {
		if ("Fail".equals(status))
			Report.updateTestLog(action, desc, Status.FAIL);
		else if ("Pass".equals(status))
			Report.updateTestLog(action, desc, Status.PASS);
		else
			Report.updateTestLog(action, desc, Status.DONE);
	}

	/*
	 * Function Name : referenceVal Description : To return the Reference cell
	 * 
	 */

	public String referenceVal(String cellVal) {
		try {
			String ExpectedValue;
			// To handle inputs referring other column values
			if ((cellVal.trim().substring(0, 1).contains("~")) && (cellVal.trim().contains(":"))) {
				System.out.println("Reference Cell");
				String[] arrReference = cellVal.split(":");
				String Ref_Scenario, Ref_Flow, Ref_Iteration, Ref_SubIteration;
				// To remove "~" in the 1st character
				String ExpectedSheet = arrReference[0].substring(1, arrReference[0].length());
				String ExpectedColumn = arrReference[1];

				System.out.println(ExpectedSheet + ":" + ExpectedColumn);

				switch (arrReference.length) {
				case 2:
					// Eg: Login:UserName
					ExpectedValue = referenceVal(userData.getData(ExpectedSheet, ExpectedColumn));
					break;

				case 4:
					// Eg: Login:UserName:1:2
					Ref_Iteration = arrReference[2];
					Ref_SubIteration = arrReference[3];
					ExpectedValue = referenceVal(
							userData.getData(ExpectedSheet, ExpectedColumn, Ref_Iteration, Ref_SubIteration));
					break;

				case 6:
					// Eg: Login:UserName:Scenario1:TestCase4:1:2
					Ref_Scenario = arrReference[2];
					Ref_Flow = arrReference[3];
					Ref_Iteration = arrReference[4];
					Ref_SubIteration = arrReference[5];
					ExpectedValue = referenceVal(userData.getData(ExpectedSheet, ExpectedColumn, Ref_Scenario, Ref_Flow,
							Ref_Iteration, Ref_SubIteration));
					break;

				default:
					ExpectedValue = cellVal;
					Report.updateTestLog(Action, "Input is in invalid format", Status.DEBUG);
				}
			} else {
				ExpectedValue = cellVal;
				System.out.println("Non Reference Cell");
			}

			System.out.println("Refernce value:" + ExpectedValue);
			return ExpectedValue;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Report.updateTestLog(Action, "Inside catch block-Error in Function", Status.DEBUG);
			return null;
		}

	}

	/**
	 * To Click OK button in popup if Exists
	 */

	public void clickOkPopupifExists() {
		try {
			String FieldName = "OK";
			highlightifExists(Driver.findElement(getButton(FieldName)));
			clickIfExists(getButton(FieldName));
			sync();
			Report.updateTestLog(Action, "Clicked OK button in the popup", Status.PASS);
		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Return the 'By' of a button
	 * 
	 * @param FieldName
	 * @return
	 */

	public By getButton(String FieldName) {
		// return By.xpath(String.format("//span[contains(@id,'button-') and
		// contains(@id,'-btnInnerEl') and text()='%s']",
		// String.valueOf(FieldName)));

		return By.xpath(String.format("//span[contains(@id,'button-') and contains(@id,'-btnInnerEl') and text()='%s']",
				String.valueOf(FieldName)));
	}

	public void highlight(WebElement ele) {
		if (isElementPresent(ele)) {
			((JavascriptExecutor) Driver).executeScript("arguments[0].style.border='3px solid red'", ele);
			Report.updateTestLog(Action, "Highlighted Object", Status.PASS);
		} else
			Report.updateTestLog(Action, "Object not present", Status.FAIL);
	}

	public void highlightifExists(WebElement ele) {
		if (isElementPresent(ele)) {
			((JavascriptExecutor) Driver).executeScript("arguments[0].style.border='3px solid red'", ele);
			Report.updateTestLog(Action, "Highlighted Object", Status.PASS);
		} else
			Report.updateTestLog(Action, "Object not present", Status.DONE);
	}

	@Action(object = ObjectType.BROWSER, desc = "Accept the alert present")
	public void GWacceptAlertifExists() {
		try {
			acceptAlertifExists();
			Report.updateTestLog(Action, "Alert Accepted", Status.PASS);

		} catch (Exception e) {
			Report.updateTestLog(Action, e.getMessage(), Status.FAIL);

		}
	}

	// ----------- PDF Validation -----------//

	@Action(desc = "Read pdf <Data> content and store it in datasheet <condition> ")
	public void exportPDFContent() {
		// pdfValidation pdfValidation = new pdfValidation();
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		String parsedText = null;

		try {

			// Get relative path
			String relativePath = System.getProperty("user.dir");
			System.out.println("relativePath:" + relativePath);

			String fileName = referenceVal(userData.getData("Document", "FileName"));
			String filePath = relativePath + "\\Projects\\MWAutoProject\\Documents\\" + fileName + ".pdf";
			System.out.println("Opening : " + filePath);

			File file = new File(filePath);
			PDFParser parser = new PDFParser(new FileInputStream(file));

			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();

			pdDoc = new PDDocument(cosDoc);
			int totalpages = pdDoc.getNumberOfPages();
			pdfStripper.setStartPage(1);
			pdfStripper.setEndPage(totalpages);
			parsedText = pdfStripper.getText(pdDoc);

			System.out.println("Full Content : " + parsedText);

			if (parsedText != "") {
				userData.putData("Doc_StaticFields", "FullContent", parsedText);
				Report.updateTestLog(Action, "PDF Content exported successfully", Status.PASS);
			} else {
				Report.updateTestLog(Action, "Unable to read PDF Content", Status.FAIL);
			}

		} catch (MalformedURLException e2) {
			System.err.println("URL string could not be parsed " + e2.getMessage());
		} catch (IOException e) {
			System.err.println("Unable to open PDF Parser. " + e.getMessage());
			try {
				if (cosDoc != null)
					cosDoc.close();
				if (pdDoc != null)
					pdDoc.close();
			} catch (Exception e1) {
				e.printStackTrace();
			}
		}

		System.out.println("+++++++++++++++++");
		System.out.println(parsedText);
		System.out.println("+++++++++++++++++");
		// pdfValidation.verifyPDFFile();
	}

	/******************************
	 * Insurer IG/
	 ******************************************/

	@Action(desc = "To Handle Sync", input = InputType.OPTIONAL)
	public void IGsync() {
		if (!Data.isEmpty() && Data != null) {
			int MaxWaitMilliSeconds = Integer.parseInt(Data);
			syncIG(MaxWaitMilliSeconds);
		} else
			syncIG();
	}

	/**
	 * To Handle browser Synchronization. It will allow the script to wait till
	 * page load is complete
	 */
	public void syncIG() {
		try {
			sleep(SYNC_WAIT_MILLISECONDS);
			WebElement loading = find(LOADING_DIV_IG);
			// new WebDriverWait(Driver,
			// getWaitTime()).until(ExpectedConditions.invisibilityOf(loading));
			new WebDriverWait(Driver, DEFAULT_WAIT_TIME).until(ExpectedConditions.invisibilityOf(loading));
			Thread.sleep(500);
		} catch (Exception ex) {

		}
	}

	/**
	 * To Handle browser Synchronization. It will allow the script to wait till
	 * page load is complete
	 */
	public void syncIG(int MAX_WAIT_MILLISECONDS) {
		try {
			sleep(SYNC_WAIT_MILLISECONDS);
			WebElement loading = find(LOADING_DIV_IG);
			new WebDriverWait(Driver, MAX_WAIT_MILLISECONDS).until(ExpectedConditions.invisibilityOf(loading));
			Thread.sleep(1000);
		} catch (Exception ex) {

		}
	}

	/**
	 * To Select List value from drop down
	 */
	@Action(object = ObjectType.SELENIUM, desc = "To Select List value from drop down", input = InputType.YES, condition = InputType.OPTIONAL)
	public void IGSelectListItem() {
		try {

			String ListItem_XPath = "";

			if (!Condition.isEmpty() && Condition != null) {
				ListItem_XPath = Condition;
			} else {
				ListItem_XPath = "//*[@class='dhx_combo_list']//*[text()='" + Data + "']";
			}

			// To get the Reference cell value
			Data = referenceVal(Data);

			String type = Element.getTagName();

			syncIG();

			if ("select".equalsIgnoreCase(type)) {
				new Select(Element).selectByVisibleText(Data);
				syncIG();
				Report.updateTestLog(Action, "Selected " + Data + " from Listbox", Status.PASS);
			}

			else if ("input".equalsIgnoreCase(type)) {

				if (isElementPresent(Element)) {
					Element.click();
					Thread.sleep(500);

					Driver.findElement(By.xpath(ListItem_XPath)).click();
					syncIG();
					Report.updateTestLog(Action, "Selected " + Data + " from Listbox", Status.PASS);

				} else
					Report.updateTestLog(Action, "Listbox Element not present", Status.FAIL);
			}
		} catch (Exception ex) {
			Report.updateTestLog(Action, "Exception in IGSelectListItem. " + ex.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "To clear previous work list item and navigate to home page")
	public void IGclearAll() {
		try {
			By ClearAll = By.xpath("//*[@id='btnSubmitActions'][text()='Clear all']");
			By lnk_AddNewPolicy = By.xpath("//a[text()='Add new policy']");

			if (!isElementPresent(lnk_AddNewPolicy)) {
				if (isElementPresent(ClearAll)) {
					executeTestCase("InsurerIG_NB", "IG_ClearAll");
				}
			}
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "To Set value in editbox by adding <Data> to the current value in that editbox", input = InputType.YES)
	public void IGSet_add() {
		try {

			int currentValue = Integer.parseInt(Element.getAttribute("value"));
			int setValue = (currentValue + Integer.parseInt(Data));
			String strValue = "";
			strValue = Integer.toString(setValue);
			IGsync();
			executeMethod(Element, "Set", strValue);
			IGsync();
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "To export the Policy Number Format 2")
	public void IGexportPolicyNumber_Format2() {
		try {

			String policyNumber = userData.getData("IG_Search", "PolicyNumber");
			String policyNumber_Format2 = policyNumber.substring(3);
			userData.putData("IG_Search", "PolicyNumber_Format2", policyNumber_Format2);
			Report.updateTestLog(Action,
					"Policy Number Formatted as '" + policyNumber_Format2 + "' and export to Data sheet", Status.PASS);
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception. " + e.getMessage(), Status.FAIL);
		}
	}

	/**
	 * To Set value in editbox present in Web table
	 * 
	 */
	@Action(object = ObjectType.SELENIUM, desc = "Enter the value [<Data>] in the Field [<Object>]", input = InputType.YES, condition = InputType.OPTIONAL)
	public void GWSet_WebTbl() {
		if (Data != null && !Data.isEmpty()) {
			try {
				// To get the Reference cell value
				Data = referenceVal(Data);

				if (!Condition.isEmpty() && Condition != null) {
					byFocusInput = By.xpath(Condition);
				}
				waitforElementPresent(Element);

				if (isElementPresent(Element)) {
					if (Element.isEnabled()) {
						click(Element);
						System.out.println("Element Clicked");
						sleep(500);
						// waitforElementPresent(Driver.findElement(byFocusInput));
						System.out.println("byFocusInput present");
						WebElement eleFocus = Driver.findElement(byFocusInput);
						clickClearSet(eleFocus, Data);
						System.out.println("value set in byFocusInput");

						Report.updateTestLog(Action, "Entered Text '" + Data + "' in '" + ObjectName + "'",
								Status.PASS);
					} else {
						Report.updateTestLog(Action, ObjectName + " Element not Enabled", Status.FAIL);
						throw new RuntimeException("Element not Enabled" + ObjectName);
					}
				} else {
					Report.updateTestLog(Action, ObjectName + " Element not Present", Status.FAIL);
					throw new RuntimeException("Element not Present" + ObjectName);
				}
			} catch (Exception ex) {
				throw new RuntimeException("Exception in method " + ex.getMessage());
			}
		}
	}

	/**
	 * To wait for the element to be present till the given wait time Parameters
	 * 
	 * @param by
	 *            locator wait time in seconds
	 */
	public void waitforElementPresent(WebElement ele) {
		try {
			sync();
			WebDriverWait wait = new WebDriverWait(Driver, DEFAULT_WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOf(ele));
		} catch (Exception ex) {
			throw new RuntimeException("Error in Waiting for Element" + ele.getText(), ex);
		}
	}

	/**
	 * Click on element
	 * 
	 * @param by
	 *            locator
	 */
	public void click(WebElement ele) {
		try {
			if (isElementPresent(ele)) {
				new WebDriverWait(Driver, 5).ignoring(StaleElementReferenceException.class, WebDriverException.class)
				.until(driver -> {
					ele.click();
					return true;
				});

			} else {
				throw new RuntimeException("Element not present " + ele);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Error in click " + ele.getText(), ex);
		}

	}

	@Action(desc = "To do all intial setup")
	public void initSetup() {
		DEFAULT_WAIT_TIME = SystemDefaults.waitTime.get();
		Report.updateTestLog(Action, "DEFAULT_WAIT_TIME Set to: " + DEFAULT_WAIT_TIME, Status.DONE);
		DEFAULT_ELEMENT_WAIT_TIME = SystemDefaults.elementWaitTime.get();
		Report.updateTestLog(Action, "DEFAULT_ELEMENT_WAIT_TIME is set to: " + DEFAULT_ELEMENT_WAIT_TIME, Status.DONE);
	}

	@Action(object = ObjectType.SELENIUM, desc = "Store the [<Object>] element's text into datasheet:columname [<Data>]. Start index [begin with '0'] and End Index of the element's text can be mentiond in [<Condition>] like '3:5'", input = InputType.YES, condition = InputType.OPTIONAL)
	public void GW_storeTextinDataSheet() {
		if (elementPresent()) {
			String strObj = Input;
			if (strObj.matches(".*:.*")) {
				try {
					System.out.println("Updating value in SubIteration " + userData.getSubIteration());
					String sheetName = strObj.split(":", 2)[0];
					String columnName = strObj.split(":", 2)[1];
					String elText = getElementText();

					if (Condition != null && !Condition.isEmpty()) {

						String[] strindex = Condition.split(":");

						strindex = Arrays.copyOf(strindex, strindex.length + 1);

						if (strindex[0].isEmpty())
							strindex[0] = "0";

						if (strindex[1] == null)
							strindex[1] = Integer.toString(elText.length());

						int beginIndex = Integer.parseInt(strindex[0]);
						int endIndex = Integer.parseInt(strindex[1]);
						elText = elText.substring(beginIndex, endIndex);
					}
					userData.putData(sheetName, columnName, elText.trim());
					Report.updateTestLog(Action, "Element text [" + elText + "] is stored in " + strObj, Status.PASS);
				} catch (Exception ex) {
					Logger.getLogger(this.getClass().getName()).log(Level.OFF, ex.getMessage(), ex);
					Report.updateTestLog(Action, "Error Storing text in datasheet " + ex.getMessage(), Status.DEBUG);
				}

			} else {
				Report.updateTestLog(Action,
						"Given input [" + Input + "] format is invalid. It should be [sheetName:ColumnName]",
						Status.FAIL);
			}
		} else {
			throw new ElementException(ExceptionType.Element_Not_Found, ObjectName);
		}
	}

	private String getElementText() {
		if (Element.getTagName().equalsIgnoreCase("input") || Element.getTagName().equalsIgnoreCase("textarea")) {
			return Element.getAttribute("value");
		} else if (Element.getTagName().equalsIgnoreCase("select")) {
			return new Select(Element).getFirstSelectedOption().getText();
		} else {
			return Element.getText();
		}
	}

	@Action(desc = "To reset the default Wait time for all wait actions.Once the default wait time is changed using this action, all the wait actions used subsequently will have an explicit timeout for that duration")
	public void resetWaitTime() {
		executeMethod("changeWaitTime", Integer.toString(DEFAULT_WAIT_TIME));
		Report.updateTestLog(Action, "Wait Time reset to default time: " + DEFAULT_WAIT_TIME, Status.DONE);
	}

	@Action(desc = "To reset the default element Wait time taken to find an object in your application during execution. Once the default time is changed using this action, for each step following that action will try to find the object within the specified time duration before performing the respective action on that object")
	public void resetElementTimeOut() {
		executeMethod("setElementTimeOut", Integer.toString(DEFAULT_ELEMENT_WAIT_TIME));
		Report.updateTestLog(Action, "Wait Time reset to default time: " + DEFAULT_WAIT_TIME, Status.DONE);
	}

	@Action(desc = "To Store the current time and date in the <Input>", input = InputType.YES)
	public void storeCurrentTime() {

		String strObj = Input;
		if (strObj.matches(".*:.*")) {
			try {
				System.out.println("Updating value in SubIteration " + userData.getSubIteration());
				String sheetName = strObj.split(":", 2)[0];
				String columnName = strObj.split(":", 2)[1];

				// Create object of SimpleDateFormat class and decide the format
				DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				// get current date time with Date()
				Date date = new Date();

				// Now format the date
				String currentTime = dateFormat.format(date);

				userData.putData(sheetName, columnName, currentTime);
				Report.updateTestLog(Action, "Current System time [" + currentTime + "] is stored in " + strObj,
						Status.PASS);
			} catch (Exception ex) {
				Logger.getLogger(this.getClass().getName()).log(Level.OFF, ex.getMessage(), ex);
				Report.updateTestLog(Action, "Error Storing text in datasheet " + ex.getMessage(), Status.DEBUG);
			}

		} else {
			Report.updateTestLog(Action,
					"Given input [" + Input + "] format is invalid. It should be [sheetName:ColumnName]", Status.DEBUG);
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "To Wait Till 'Invoice Status' object with expected status appears.")
	public void WaitTill_InvoiceStatusChange() {
		try {
			int iLoop = 1;

			Thread.sleep(2000);
			sync();
			while (iLoop <= 10) {

				// Navigate to Charges Screen
				AObject.findElement("pane_Charges", "GWBC-LeftMenu").click();
				sync();
				Thread.sleep(2000);
				Report.updateTestLog(Action, "Navigate to Charges Screen", Status.PASS);

				// Navigate to Invoice Screen
				AObject.findElement("pane_Invoices", "GWBC-LeftMenu").click();
				sync();
				Thread.sleep(2000);
				Report.updateTestLog(Action, "Navigate to Invoice Screen", Status.PASS);

				waitforElementPresent(AObject.findElement("title_Invoices", "GWBC-Invoices"));
				sync();
				Thread.sleep(2000);

				// Check Status and exit loop if condition Passed
				if (isElementPresent(Element)) {
					Report.updateTestLog(Action, "Expected element" + Element + " present", Status.PASS);
					break;
				}

				iLoop = iLoop + 1;
			}

		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.DONE);

			try {
				Thread.sleep(20000);
				Report.updateTestLog(Action, "Hard Wait for 20 seconds to allow the invoice status change",
						Status.PASS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Navigate to Invoice Screen
			AObject.findElement("pane_Invoices", "GWBC-LeftMenu").click();
			sync();
			Report.updateTestLog(Action, "Navigate to Invoice Screen due to exception", Status.PASS);

		}
	}

	@Action(desc = "To get current Date in <MM/dd/yyyy> format and store it in data sheet [BatchJobs:CurrentDate]")
	public void GWgetCurrentDate() {
		try {

			String label_CurrentDate_XPath = "//span[contains(@id,':CurrentDate-btnInnerEl')]/span[@class='infobar_elem_val']";

			// Get Current date and export it to data sheet
			waitforElementPresent((By.xpath(label_CurrentDate_XPath)));
			String currentDate = Driver.findElement(By.xpath(label_CurrentDate_XPath)).getText();

			System.out.println("Date:" + currentDate);
			SimpleDateFormat dtFrmt = new SimpleDateFormat("MMM dd, yyyy");
			SimpleDateFormat GWdtFrmt = new SimpleDateFormat("MM/dd/yyyy");
			Date date1 = new Date();
			date1 = dtFrmt.parse(currentDate);
			String date2 = GWdtFrmt.format(date1);

			System.out.println("Formatted Date:" + date2);

			userData.putData("BatchJobs", "CurrentDate", date2);
			userData.putData("AppURL", "CurrentDate", date2);

			Report.updateTestLog(Action,
					"Exported current application date as '" + date2 + "' in datatable <BatchJobs:CurrentDate>",
					Status.PASS);

		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
			userData.putData("BatchJobs", "CurrentDate", "");
			userData.putData("AppURL", "CurrentDate", "");
		}
	}

	@Action(desc = "To export move Clock command based on input from <ChangeDate>,<DaytoAdd> from Datasheet 'BatchJobs'")
	public void GWExportClock_command() {
		try {

			// Get Current date and date to be changed
			String strCurrentDate = referenceVal(userData.getData("BatchJobs", "CurrentDate"));
			String strChangeDate = referenceVal(userData.getData("BatchJobs", "ChangeDate"));

			// Get the difference between two dates
			SimpleDateFormat GWdtFrmt = new SimpleDateFormat("MM/dd/yyyy");
			Date currentDate = GWdtFrmt.parse(strCurrentDate);
			Date changeDate = GWdtFrmt.parse(strChangeDate);

			if (currentDate.compareTo(changeDate) <= 0) {
				Long daysDiff = changeDate.getTime() - currentDate.getTime();

				Long diffInDays = TimeUnit.MILLISECONDS.toDays(daysDiff);

				int diff = diffInDays.intValue();

				System.out.println("Days difference:" + diff);

				int intaddDays;

				// Get DaytoAdd from Data table (if applicable for the current
				// test iteration)
				String strDaysToAdd = userData.getData("BatchJobs", "DaytoAdd");

				if (strDaysToAdd != null && !strDaysToAdd.isEmpty()) {
					int intDaysToAdd = Integer.parseInt(strDaysToAdd);
					intaddDays = diff + intDaysToAdd;
				} else
					intaddDays = diff;

				String cmd = "Run Clock addDays " + intaddDays;

				// export the Clock Move command with days difference
				userData.putData("BatchJobs", "cmd_clk", cmd);

				Report.updateTestLog(Action,
						"Exported Clock Move command days as '" + cmd + "' in datatable <BatchJobs:cmd_clk>",
						Status.PASS);

			}

			else {
				Report.updateTestLog(Action, "Cannot move clock to past date. Current date: " + currentDate
						+ " ; Change date: " + changeDate, Status.FAIL);
				userData.putData("BatchJobs", "cmd_clk", "");
			}

		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
			userData.putData("BatchJobs", "cmd_clk", "");
		}
	}

	@Action(desc = "To export Batch Process command based on input from <BatchName> from Datasheet 'BatchJobs'")
	public void GWExportBatch_command() {
		try {

			// Get Current date and date to be changed
			String strBatchName = referenceVal(userData.getData("BatchJobs", "BatchName"));
			strBatchName = strBatchName.replace(" ", "");

			String cmd = "RunBatchProcess " + strBatchName;

			// export the Clock Move command with days difference
			userData.putData("BatchJobs", "cmd_batchprocess", cmd);

			Report.updateTestLog(Action,
					"Exported Batch Process command as '" + cmd + "' in datatable <BatchJobs:cmd_batchprocess>",
					Status.PASS);

		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
			userData.putData("BatchJobs", "cmd_batchprocess", "");
		}
	}

	@Action(object = ObjectType.SELENIUM, desc = "Execute Resuable mentioned in the Input Column in the format <Scenario:TestCase> if the object Exists", input = InputType.YES)
	public void GW_ExecuteReusable_IfExists() {
		if (isElementPresent(Element)) {
			String[] scenario = Data.split(":");
			executeTestCase(scenario[0], scenario[1]);
		}
	}

	@Action(desc = "Select Next Monday Date from given date")
	public void GW_ExecuteNextMondayDate() throws ParseException {
		// String Data= "8/20/2019";
		// String input_date="01/08/2012";
		// SimpleDateFormat format1=new SimpleDateFormat("dd/MM/yyyy");
		// Date dt1=format1.parse(input_date);
		// DateFormat format2=new SimpleDateFormat("EEEE");
		// String finalDay=format2.format(dt1);

		Calendar date1 = Calendar.getInstance();
		date1.set(2019, 07, 21);

		if (date1.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
			date1.add(Calendar.DATE, 1);
		}

		while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
			date1.add(Calendar.DATE, 1);
		}

		System.out.println(date1.getTime());

	}
	
	
	@Action(object = ObjectType.SELENIUM, desc = "To click on checkbox object")
	public void GWclickCheckbox() {
		try {
			String currentBrowser = Browser.fromString(getCurrentBrowserName()).toString();
			
			if (isElementPresent(Element)) {
				Element.click();
				Thread.sleep(1000);
				if (currentBrowser.equals("IE")) {
					Element.sendKeys(Keys.SPACE);
				}
				Report.updateTestLog(Action, "Clicked on checkbox " + Element.getText(), Status.PASS);
			} else
				Report.updateTestLog(Action, "Element not Present " + Element.getText(), Status.FAIL);
		}

		catch (StaleElementReferenceException stale) {
			try {
				WebElement Element = AObject.findElement(ObjectName, Reference);
				if (isElementPresent(Element)) {
					Element.click();
					Report.updateTestLog(Action, "Clicked on checkbox " + Element.getText(), Status.PASS);
				} else {
					stale.printStackTrace();
					Report.updateTestLog(Action, "Exception in method. " + stale.getMessage(), Status.FAIL);
				}
			} catch (StaleElementReferenceException e) {
				Report.updateTestLog(Action, "Exception handled in GWclickCheckbox." + e.getMessage(), Status.DONE);
			}

		}

		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
		}

	}

}
