package gw;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.cognizant.cognizantits.engine.commands.General;
import com.cognizant.cognizantits.engine.core.CommandControl;
import com.cognizant.cognizantits.engine.support.Status;
import com.cognizant.cognizantits.engine.support.methodInf.Action;
import com.cognizant.cognizantits.engine.support.methodInf.InputType;

public class BusinessFlow extends General {
	public BusinessFlow(CommandControl cc) {
		super(cc);
	}

	Guidewire gw = new Guidewire(getCommander());

	@Action(desc = "To check whether 11 or 12 invoices generated and call relevant method")
	public void TC007_ValidateInvoiceTerms_ChargesScreen() {
		By LABEL_AMOUNT_INVOICETERMS_ALL = By.cssSelector("[id*=':InvoiceItemsLV-body'] table td:nth-child(10)>div");

		int sizeInvoice = Driver.findElements(LABEL_AMOUNT_INVOICETERMS_ALL).size();

		if (sizeInvoice == 12)
			executeTestCase("BC_Smoke_TestCaseReusables", "TC007_ValidateInvoiceTerms_ChargesScreen_11");
		else if (sizeInvoice == 13)
			executeTestCase("BC_Smoke_TestCaseReusables", "TC007_ValidateInvoiceTerms_ChargesScreen_12");
	}

	@Action(desc = "To check whether 11 or 12 invoices generated and call relevant method")
	public void TC007_ValidateInvoiceDue_InvoiceScreen() {
		By LABEL_DUE_INVOICE_ALL = By.xpath("//*[contains(@id,':AccountInvoicesLV')]//table//td[9]/div");

		int sizeInvoice = Driver.findElements(LABEL_DUE_INVOICE_ALL).size();
		int row = 1;
		String InvDueRow1_XPath = "//*[contains(@id,':AccountInvoicesLV-body')]//table[" + row
				+ "]//td[count(//div[contains(@id,':AccountInvoicesLV')]/div[contains(@id,'headercontainer')]//div/span/span[.='Due']/../../../preceding-sibling::div)+1]/div";
		String InvDueRow1 = Driver.findElement(By.xpath(InvDueRow1_XPath)).getText();
		if (sizeInvoice == 12) {
			if (InvDueRow1.equals("$1,200.01"))
				executeTestCase("BC_Smoke_TestCaseReusables", "TC007_ValidateInvoiceDue_InvoiceScreen_11_1");
			else if (InvDueRow1.equals("$2,109.10"))
				executeTestCase("BC_Smoke_TestCaseReusables", "TC007_ValidateInvoiceDue_InvoiceScreen_11_2");
			else
				Report.updateTestLog(Action, "Invoice Due amount in row 1 not matching with data table values",
						Status.FAIL);
		} else if (sizeInvoice == 13)
			executeTestCase("BC_Smoke_TestCaseReusables", "TC007_ValidateInvoiceDue_InvoiceScreen_12");
	}

	@Action(desc = "To validate Filter option in My Delinquencies screen by picking a valid Producer Code")
	public void FilterByProducerCode() {
		try {

			String ProducerCode = getProducerCode();
			if (ProducerCode != "") {
				Report.updateTestLog(Action, "Valid Producer Code '" + ProducerCode
						+ "' found to validate the Filter by Producer Code functionality", Status.PASS);

				userData.putData("Search", "Filter_ProducerCode", ProducerCode);
				executeTestCase("BillingCenter_Desktop", "FilterByProducerCode");

			} else {
				userData.putData("Search", "Filter_ProducerCode", "");
				Report.updateTestLog(Action,
						"No Valid Producer Code found. So unable to validate the Filter by Producer Code functionality",
						Status.DONE);
			}
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception in FilterByProducerCode. " + e.getMessage(), Status.FAIL);

		}

	}

	@Action(desc = "To validate Filter option in My Delinquencies screen by picking a valid Producer Code", input = InputType.YES)
	public void validateFilterByProducerCode() {
		try {

			int flag = 1;
			String errProducerCode = "";

			String ProducerCode = Data;
			if (ProducerCode != "") {

				By ProducerCode_CSS = By.cssSelector("[id*='DesktopDelinquenciesLV-body'] td:nth-child(13)");

				List<WebElement> lstProducerCode = Driver.findElements(ProducerCode_CSS);

				for (WebElement ele : lstProducerCode) {
					System.out.println("Producer Code: " + ele.getText());
					if (!(Data.trim().equals(ele.getText()))) {
						flag = 0;
						errProducerCode = ele.getText();
						break;
					}

				}

				if (flag == 1)
					Report.updateTestLog(Action, "validate Filter by Producer code '" + ProducerCode + "' works fine",
							Status.PASS);
				else
					Report.updateTestLog(Action, "validate Filter by Producer code '" + ProducerCode
							+ "' is not working fine. Wrong Producer code '" + errProducerCode + "' is displayed",
							Status.FAIL);

			} else {
				Report.updateTestLog(Action,
						"No Valid Producer Code found. So unable to validate the Filter by Producer Code functionality",
						Status.DONE);
			}
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception in validateFilterByProducerCode. " + e.getMessage(), Status.FAIL);

		}

	}

	public String getProducerCode() {
		try {
			String ProducerCode_XPath = "//*[contains(@id,'DesktopDelinquenciesLV-body')]//table//td[13]/div";
			String NextPage_XPath = "//*[contains(@class,'-page-next')]";
			String TotalPages_XPath = "//*[contains(@id,'_ListPaging')]/../../following-sibling::div[1]";

			String ProducerCode = "", strtotalPages = "";
			int flag = 0;
			int totalPages = 1;

			// Get the Total pages count
			gw.waitforElementPresent(By.xpath(TotalPages_XPath));
			strtotalPages = Driver.findElement(By.xpath(TotalPages_XPath)).getText();
			if (strtotalPages != "") {
				String[] arrtotalPages = strtotalPages.split(" ");
				totalPages = Integer.parseInt(arrtotalPages[1]);
			}
			System.out.println("Total Pages:" + totalPages);

			// Iterate each page to get valid producer code
			for (int iPage = 1; iPage <= totalPages; iPage++) {

				gw.waitforElementPresent(By.xpath(ProducerCode_XPath));
				List<WebElement> lstAllProducerCodes = Driver.findElements(By.xpath(ProducerCode_XPath));
				// Get rows count
				int totalRows = lstAllProducerCodes.size();

				for (WebElement elm : lstAllProducerCodes) {
					if (!elm.getText().trim().isEmpty()) {
						ProducerCode = elm.getText();
						flag = 1;
						break;
					}

				}
				if (flag == 1)
					break;
				else if (iPage < totalPages && iPage < 5) {
					WebElement nextButton = Driver.findElement(By.xpath(NextPage_XPath));
					gw.sync();
					gw.waitforElementPresent(By.xpath(NextPage_XPath));
					nextButton.click();
					Report.updateTestLog(Action,
							"Navigating to next page (Page - " + (iPage + 1) + ") to get valid producer code",
							Status.PASS);
					gw.sync();
					Thread.sleep(1000);
				} else
					break;

			}

			return ProducerCode;
		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in getting valid Producer Code. " + e.getMessage(), Status.FAIL);
			return "";
		}

	}

	@Action(desc = "To validate Sort option in My Group Activities screen by sorting 'Opened' column value")
	public void validateSort() {
		try {

			String link_Opened_arrow_XPath = "//*[contains(@id,':DesktopMyGroupActivitiesLV')]//div[contains(@class,'header')]//div//*[text()='Opened']/../../following-sibling::div";
			String link_SortAscending_XPath = "//*[text()='Sort Ascending']/following-sibling::div";
			String link_SortDescending_XPath = "//*[text()='Sort Descending']/following-sibling::div";
			String Opened_Date_1_XPath = "//*[contains(@id,'DesktopMyGroupActivitiesLV-body')]//table[1]//td[4]/div";
			String strAscendingVal = "", strDescendingVal = "";
			Date dtAscending = new Date();
			Date dtDescending = new Date();

			WebElement link_Opened_arrow = Driver.findElement(By.xpath(link_Opened_arrow_XPath));

			// Click on Sort Ascending and Get the date from the 1st row
			gw.sync();
			Thread.sleep(1000);

			// gw.waitforElementPresent(By.xpath(link_Opened_arrow_XPath));
			// gw.clickByJS(By.xpath(link_Opened_arrow_XPath));

			executeMethod(link_Opened_arrow, "waitForElementToBePresent");
			executeMethod(link_Opened_arrow, "clickByJS");

			WebElement link_SortAscending = Driver.findElement(By.xpath(link_SortAscending_XPath));
			gw.waitforElementPresent(By.xpath(link_SortAscending_XPath));
			gw.click(By.xpath(link_SortAscending_XPath));
			gw.sync();
			Thread.sleep(1000);
			gw.sync();

			gw.waitforElementPresent(By.xpath(Opened_Date_1_XPath));
			strAscendingVal = Driver.findElement(By.xpath(Opened_Date_1_XPath)).getText();

			// Click on Sort Descending and Get the date from the 1st row
			gw.sync();
			Thread.sleep(1000);
			gw.waitforElementPresent(By.xpath(link_Opened_arrow_XPath));
			gw.click(By.xpath(link_Opened_arrow_XPath));
			WebElement link_SortDescending = Driver.findElement(By.xpath(link_SortDescending_XPath));
			gw.waitforElementPresent(By.xpath(link_SortDescending_XPath));
			gw.click(By.xpath(link_SortDescending_XPath));
			gw.sync();
			Thread.sleep(1000);
			gw.sync();
			gw.waitforElementPresent(By.xpath(Opened_Date_1_XPath));
			strDescendingVal = Driver.findElement(By.xpath(Opened_Date_1_XPath)).getText();

			// Check whether DescendingVal > AscendingVal
			SimpleDateFormat dtFrmt = new SimpleDateFormat("MM/dd/yyyy");
			dtAscending = dtFrmt.parse(strAscendingVal);
			dtDescending = dtFrmt.parse(strDescendingVal);

			if (dtAscending.before(dtDescending))
				Report.updateTestLog(Action, "Sorting functionality works fine. Opened column Min. date = "
						+ dtAscending + " Opened column Max. date = " + dtDescending, Status.PASS);
			else
				Report.updateTestLog(Action, "Sorting functionality not working fine. Min. date = " + dtAscending
						+ " Max. date = " + dtDescending, Status.FAIL);

		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception in validateSort. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "To check whether Date 1 <Input> LessThan Date 2 <Condition>", input = InputType.YES, condition = InputType.YES)
	public void isDateLessThan() {
		try {

			String strAscendingVal = "", strDescendingVal = "";
			Date dtAscending = new Date();
			Date dtDescending = new Date();

			strAscendingVal = Data;

			if (Condition.startsWith("%") && Condition.endsWith("%"))
				strDescendingVal = getVar(Condition);
			else
				strDescendingVal = Condition;

			// Check whether DescendingVal > AscendingVal
			SimpleDateFormat dtFrmt = new SimpleDateFormat("MM/dd/yyyy");
			dtAscending = dtFrmt.parse(strAscendingVal);
			dtDescending = dtFrmt.parse(strDescendingVal);

			if (dtAscending.before(dtDescending))
				Report.updateTestLog(Action, "Sorting functionality works fine. Opened column Min. date = "
						+ dtAscending + " Opened column Max. date = " + dtDescending, Status.PASS);
			else
				Report.updateTestLog(Action, "Sorting functionality not working fine. Min. date = " + dtAscending
						+ " Max. date = " + dtDescending, Status.FAIL);

		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in validateSort. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "To check whether no error is triggered on clicking document download button")
	public void verifyDocDownload() {
		try {
			String FieldName = "OK";
			String ErrorMsg = "Could not retrieve document from the Document Management System. Please contact administrator";

			By OK_Popup = gw.getButton(FieldName);
			By Error = By.xpath("//*[contains(text(),'" + ErrorMsg + "')]");

			if (gw.isExists(OK_Popup)) {
				gw.sync();
				Report.updateTestLog(Action, "Error in document download", Status.FAIL);
				gw.find(OK_Popup).click();

			} else if (gw.isExists(Error)) {
				Report.updateTestLog(Action, "Error in document download", Status.FAIL);
			} else {
				Report.updateTestLog(Action, "Document Download button clicked", Status.PASS);
			}
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "To select the Policy row in the table", input = InputType.YES)
	public void IGSelectPolicyRow() {
		try {
			String policyNumber = gw.referenceVal(Data);
			int flagFound = 0;
			// String ReferenceNo_XPath = "//*[@id='tblInbox']//tr[@class='even'
			// or @class ='odd']//td[5][contains(text(),'" + policyNumber +
			// "')]";
			String ReferenceNo_XPath = "//*[@id='tblInbox']//tr//td[5][contains(text(),'" + policyNumber + "')]";
			String NextPage_XPath = "//input[@id='btnNext_tblInbox']";
			String PrevPage_XPath = "//input[@id='btnPrev_tblInbox']";
			String LastPage_XPath = "//input[@id='btnLast_tblInbox']";
			String totalPages_XPath = "//span[@id='pgspan_tblInbox']";
			String DueDate_XPath = "//th[@id='DUEDATETIME']/a";

			Boolean existsFlag = false;

			gw.syncIG();
			executeMethod("waitForPageLoaded");

			/*
			 * //Sort the rows based on Due date WebElement dueDate =
			 * Driver.findElement(By.xpath(DueDate_XPath)); dueDate.click();
			 * Thread.sleep(2000); dueDate.click(); Thread.sleep(2000);
			 */

			// Navigate to Last page and traverse backwards
			Driver.findElement(By.xpath(LastPage_XPath)).click();
			gw.syncIG();
			executeMethod("waitForPageLoaded");
			Thread.sleep(4000);

			String strTotalPages = Driver.findElement(By.xpath(totalPages_XPath)).getText();
			int totalPages = Integer.parseInt(strTotalPages);

			Report.updateTestLog(Action, "Total Pages is '" + totalPages + "'", Status.PASS);

			for (int i = totalPages; i >= 1; i--) {

				try {
					WebElement policyRow = Driver.findElement(By.xpath(ReferenceNo_XPath));
					policyRow.click();
					if (policyRow != null)
						existsFlag = true;
				} catch (Exception exp) {
					existsFlag = false;
				}
				if (existsFlag) {
					Report.updateTestLog(Action, "Appropriate row with Reference No. '" + policyNumber
							+ "' found. XPath: " + ReferenceNo_XPath, Status.PASS);

					gw.syncIG();
					Thread.sleep(3000);
					Report.updateTestLog(Action,
							"Appropriate row with Reference No. '" + policyNumber + "' is selected", Status.PASS);
					flagFound = 1;
					break;
				}

				if (i < totalPages) {

					Driver.findElement(By.xpath(PrevPage_XPath)).click();
					gw.syncIG();
					executeMethod("waitForPageLoaded");
					Thread.sleep(2000);

					String currentPage_XPath = "//select[@id='slcPages_tblInbox']/option[@selected][text()='" + (i)
							+ "']";
					gw.waitforElementPresent(By.xpath(currentPage_XPath));
					WebElement currentPage = Driver.findElement(By.xpath(currentPage_XPath));
					Report.updateTestLog(Action, "Navigated to page: " + currentPage.getText(), Status.PASS);
				}
			}

			if (flagFound == 0) {
				Report.updateTestLog(Action, "Appropriate row with Reference No. '" + policyNumber
						+ "' is not found. XPath: " + ReferenceNo_XPath, Status.FAIL);
			}
		}

		catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
		}
	}

	@Action(desc = "Find a value greater than 'Data1' and 'Data2' & store it in [input] in 'RunTimeValues' values sheet", input = InputType.YES)
	public void exportBiggerValue() {
		try {
			String strObj = Input;
			Double val1, val2;
			String resultValue;
			if (strObj.matches(".*:.*")) {
				String sheetName = strObj.split(":", 2)[0];
				String columnName = strObj.split(":", 2)[1];

				// remove decimal values
				String value1 = userData.getData("RunTimeValues", "Data1").replace(",", "");
				String value2 = userData.getData("RunTimeValues", "Data2").replace(",", "");

				// Get the two value and find the biggest one
				val1 = Double.valueOf(value1);
				val2 = Double.valueOf(value2);

				if (val1 > val2)
					resultValue = Double.toString(val1 + 100);
				else
					resultValue = Double.toString(val2 + 100);

				// currency format
				// resultValue = "$" + resultValue + ".00";

				userData.putData(sheetName, columnName, resultValue);
				Report.updateTestLog(Action, resultValue + " is stored in " + strObj, Status.PASS);

			} else {
				Report.updateTestLog(Action,
						"Given input [" + Input + "] format is invalid. It should be [sheetName:ColumnName]",
						Status.FAIL);
			}
		} catch (Exception e) {
			Report.updateTestLog(Action, "Exception in method. " + e.getMessage(), Status.FAIL);
		}

	}

}
