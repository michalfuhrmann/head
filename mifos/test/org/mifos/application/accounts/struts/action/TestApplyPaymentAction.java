/**

 * TestApplyPaymentAction.java    version: xxx

 

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
package org.mifos.application.accounts.struts.action;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.business.AccountStateEntity;
import org.mifos.application.accounts.struts.actionforms.AccountApplyPaymentActionForm;
import org.mifos.application.accounts.util.helpers.AccountStates;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.master.util.helpers.MasterConstants;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.security.util.ActivityContext;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.security.util.resources.SecurityConstants;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.ResourceLoader;
import org.mifos.framework.util.helpers.TestObjectFactory;

import servletunit.struts.MockStrutsTestCase;

public class TestApplyPaymentAction extends MockStrutsTestCase{
	protected AccountBO accountBO;
	private CustomerBO center;
	private CustomerBO group;
	private UserContext userContext ;
	
	protected void setUp() throws Exception {
		super.setUp();
		try {
			setServletConfigFile(ResourceLoader.getURI(
					"WEB-INF/web.xml").getPath());
			setConfigFile(ResourceLoader.getURI(
					"org/mifos/framework/util/helpers/struts-config.xml")
					.getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		userContext = new UserContext();
		userContext.setId(new Short("1"));
		userContext.setLocaleId(new Short("1"));
		Set<Short> set = new HashSet<Short>();
		set.add(Short.valueOf("1"));
		userContext.setRoles(set);
		userContext.setLevelId(Short.valueOf("2"));
		userContext.setName("mifos");
		userContext.setPereferedLocale(new Locale("en", "US"));
		userContext.setBranchId(new Short("1"));
		userContext.setBranchGlobalNum("0001");
		request.getSession().setAttribute(Constants.USER_CONTEXT_KEY, userContext);
		addRequestParameter("recordLoanOfficerId", "1");
		addRequestParameter("recordOfficeId", "1");
		ActivityContext ac = new ActivityContext((short) 0, userContext
				.getBranchId().shortValue(), userContext.getId().shortValue());
		request.getSession(false).setAttribute("ActivityContext", ac);
		request.getSession().setAttribute(Constants.USER_CONTEXT_KEY, userContext);
		request.getSession().setAttribute(SecurityConstants.SECURITY_PARAM,"loan");
	}
	
	public void tearDown()throws Exception{
		TestObjectFactory.cleanUp(accountBO);
		TestObjectFactory.cleanUp(group);
		TestObjectFactory.cleanUp(center);
		HibernateUtil.closeSession();
		super.tearDown();
	}
	
	public void testApplyPaymentLoad(){
		accountBO = createLoanAccount();
		setRequestPathInfo("/applyPaymentAction");
		addRequestParameter("method", "load");
		addRequestParameter("input","loan");
		addRequestParameter("accountId",accountBO.getAccountId().toString());
		actionPerform();
		verifyForward(Constants.LOAD_SUCCESS);
		verifyNoActionErrors();
		assertNotNull(request.getSession().getAttribute(MasterConstants.PAYMENT_TYPE));
		AccountApplyPaymentActionForm actionForm = (AccountApplyPaymentActionForm)request.getSession().getAttribute("applyPaymentActionForm");
		assertEquals(actionForm.getAmount(),accountBO.getTotalAmountDue());
	}
	
	public void testApplyPaymentPreview(){
		setRequestPathInfo("/applyPaymentAction");
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
				addRequestParameter("receiptDate",sdf.format(new Date()));
		addRequestParameter("transactionDate",sdf.format(new Date()));
		addRequestParameter("paymentTypeId","1");

		addRequestParameter("method", "preview");
		actionPerform();
		verifyForward(Constants.PREVIEW_SUCCESS);
	}
	
	public void testApplyPaymentForLoan()throws Exception{
		accountBO = createLoanAccount();
		accountBO.setAccountState(new AccountStateEntity(AccountStates.LOANACC_BADSTANDING));
		request.getSession().setAttribute(Constants.BUSINESS_KEY,accountBO);
		setRequestPathInfo("/applyPaymentAction");
		addRequestParameter("input","loan");
		addRequestParameter("method", "applyPayment");
		addRequestParameter("receiptId","101");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
		
		addRequestParameter("receiptDate",sdf.format(new Date()));
		addRequestParameter("transactionDate",sdf.format(new Date()));
		addRequestParameter("paymentTypeId","1");
		actionPerform();
		verifyForward("loan_detail_page");
		assertEquals(new Money(), accountBO.getTotalAmountDue());
		assertEquals(0, accountBO.getTotalInstallmentsDue().size());
		assertEquals(AccountStates.LOANACC_ACTIVEINGOODSTANDING, accountBO.getAccountState().getId().shortValue());
	}
	
	public void testApplyPaymentPrevious(){
		setRequestPathInfo("/applyPaymentAction");
		addRequestParameter("method", "previous");
		actionPerform();
		verifyForward(Constants.PREVIOUS_SUCCESS);
	}
	
	public void testApplyPaymentCancel(){
		setRequestPathInfo("/applyPaymentAction");
		addRequestParameter("method", "cancel");
		addRequestParameter("input","loan");
		actionPerform();
		verifyForward("loan_detail_page");
	}
	
	private AccountBO createLoanAccount() {
		MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory
				.getMeetingHelper(1, 1, 4, 2));
		center = TestObjectFactory.createCenter("Center", Short.valueOf("13"),
				"1.1", meeting, new Date(System.currentTimeMillis()));
		group = TestObjectFactory.createGroup("Group", Short.valueOf("9"),
				"1.1.1", center, new Date(System.currentTimeMillis()));
		LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(
				"Loan", Short.valueOf("2"),
				new Date(System.currentTimeMillis()), Short.valueOf("1"),
				300.0, 1.2, Short.valueOf("3"), Short.valueOf("1"), Short
						.valueOf("1"), Short.valueOf("1"), Short.valueOf("1"),
				Short.valueOf("1"), meeting);
		return TestObjectFactory.createLoanAccount("42423142341", group, Short
				.valueOf("5"), new Date(System.currentTimeMillis()),
				loanOffering);
	}
}
