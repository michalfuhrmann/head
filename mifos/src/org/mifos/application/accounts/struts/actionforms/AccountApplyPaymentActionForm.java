/**

 * AccountApplyPaymentActionForm.java    version: xxx

 

 * Copyright (c) 2005-2006 Grameen Foundation USA

 * 1029 Vermont Avenue, NW, Suite 400, Washington DC 20005

 * All rights reserved.

 

 * Apache License 
 * Copyright (c) 2005-2006 Grameen Foundation USA 
 * 

 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 *

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the 

 * License. 
 * 
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an explanation of the license 

 * and how it is applied. 

 *

 */
package org.mifos.application.accounts.struts.actionforms;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.ValidatorActionForm;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.login.util.helpers.LoginConstants;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.framework.business.util.helpers.MethodNameConstants;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.struts.tags.DateHelper;
import org.mifos.framework.util.helpers.Money;

public class AccountApplyPaymentActionForm extends ValidatorActionForm{
	private String input;
	private String transactionDate;
	private Money amount;
	private String receiptId;
	private String receiptDate;
	private String paymentTypeId;
	private String globalAccountNum;
	private String accountId;
	

	public Money getAmount() {
		return amount;
	}
	
	public void setAmount(Money amount) {
		this.amount = amount;
	}
	
	public String getInput() {
		return input;
	}
	
	@Override
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		String methodCalled= request.getParameter(MethodNameConstants.METHOD);
		 
		 ActionErrors errors = new ActionErrors();
		if(methodCalled!=null&& methodCalled.equals("preview")){
			ActionErrors errors2 = validateDate(this.transactionDate,"transation date",request);
			if( null!=errors2 &&!errors2.isEmpty())
				errors.add(errors2);
			if( this.paymentTypeId==null||this.paymentTypeId.equals("")){
				errors.add(AccountConstants.ERROR_MANDATORY,new ActionMessage(AccountConstants.ERROR_MANDATORY,"mode of payment"));
			}
			errors2 = validateDate(this.receiptDate,"receipt date",request);
			if( null!=errors2 &&!errors2.isEmpty())
				errors.add(errors2);
			else errors=null;
		}
		
		return errors;
	}
	private ActionErrors validateDate(String date ,String fieldName,HttpServletRequest request){
		String method = request.getParameter("method");
		ActionErrors errors =new ActionErrors();
		java.sql.Date sqlDate=null;
		if( date!=null&&!date.equals("")){
			sqlDate=DateHelper.getLocaleDate(getUserLocale(request),date);
			Calendar currentCalendar = new GregorianCalendar();
			int year=currentCalendar.get(Calendar.YEAR);
			int month=currentCalendar.get(Calendar.MONTH);
			int day=currentCalendar.get(Calendar.DAY_OF_MONTH);
			currentCalendar = new GregorianCalendar(year,month,day);
			java.sql.Date currentDate=new java.sql.Date(currentCalendar.getTimeInMillis());
			if(currentDate.compareTo(sqlDate) < 0 ) {
				errors.add(AccountConstants.ERROR_FUTUREDATE,new ActionMessage(AccountConstants.ERROR_FUTUREDATE,fieldName));
			}
		}
		else
		{
			errors.add(AccountConstants.ERROR_MANDATORY,new ActionMessage(AccountConstants.ERROR_MANDATORY,fieldName));
		}
		if (null != errors && !errors.isEmpty()) {
			request.setAttribute(Globals.ERROR_KEY, errors);
			request.setAttribute("methodCalled", method);
		}
		return errors;
	}
	protected Locale getUserLocale(HttpServletRequest request) {
		Locale locale=null;
		HttpSession session= request.getSession();
		if(session !=null) {
			UserContext userContext= (UserContext)session.getAttribute(LoginConstants.USERCONTEXT);
			if(null !=userContext) {
				locale=userContext.getPereferedLocale();
				if(null==locale) {
					locale=userContext.getMfiLocale();
				}
			}
		}
		return locale;
	}
	public void setInput(String input) {
		this.input = input;
	}
	
	public String getPaymentTypeId() {
		return paymentTypeId;
	}
	
	public void setPaymentTypeId(String paymentTypeId) {
		this.paymentTypeId = paymentTypeId;
	}
	
	public String getReceiptDate() {
		return receiptDate;
	}
	
	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}
	
	public String getReceiptId() {
		return receiptId;
	}
	
	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}
	
	public String getTransactionDate() {
		return transactionDate;
	}
	
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	public String getGlobalAccountNum() {
		return globalAccountNum;
	}

	public void setGlobalAccountNum(String globalAccountNum) {
		this.globalAccountNum = globalAccountNum;
	}
}
