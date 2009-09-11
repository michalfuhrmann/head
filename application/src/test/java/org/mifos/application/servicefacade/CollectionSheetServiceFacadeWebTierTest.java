/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */
package org.mifos.application.servicefacade;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.application.accounts.loan.util.helpers.LoanAccountsProductView;
import org.mifos.application.accounts.savings.util.helpers.SavingsAccountView;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.collectionsheet.business.CollectionSheetEntryGridDto;
import org.mifos.application.collectionsheet.business.CollectionSheetEntryView;
import org.mifos.application.collectionsheet.struts.actionforms.BulkEntryActionForm;
import org.mifos.application.customer.business.CustomerView;
import org.mifos.application.customer.persistence.CustomerPersistence;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.master.persistence.MasterPersistence;
import org.mifos.application.office.business.OfficeView;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.application.personnel.business.PersonnelView;
import org.mifos.application.personnel.persistence.PersonnelPersistence;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.application.personnel.util.helpers.PersonnelLevel;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.Money;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * I test {@link CollectionSheetServiceFacadeWebTier}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionSheetServiceFacadeWebTierTest {

    // system under test (SUT)
    private CollectionSheetServiceFacadeWebTier collectionSheetServiceFacadeWebTier;

    // create test-doubles for all depended-on-components (DOC)s
    @Mock
    private OfficePersistence officePersistence;

    @Mock
    private MasterPersistence masterPersistence;
    
    @Mock
    private PersonnelPersistence personnelPersistence;
    
    @Mock
    private CustomerPersistence customerPersistence;
    
    @Mock
    private CollectionSheetService collectionSheetService;

    @Mock
    private CollectionSheetEntryGridViewAssembler collectionSheetGridViewAssembler;
    
    @Mock
    private ClientAttendanceAssembler clientAttendanceAssembler;
    
    @Mock
    private LoanAccountAssembler loanAccountAssembler;
    
    @Mock
    private CustomerAccountAssembler customerAccountAssembler;
    
    @Mock
    private SavingsAccountAssembler savingsAccountAssembler;
    
    @Mock
    private AccountPaymentAssembler accountPaymentAssembler;

    @Mock
    private MasterDataEntity masterDataEntity;
    
    @Mock
    private CollectionSheetDto collectionSheetData;
    
    @Mock
    private CollectionSheetCustomerDto centerCustomer;
    
    @Mock
    private CollectionSheetCustomerSavingDto collectionSheetSaving;
    
    @Mock
    private CollectionSheetCustomerLoanDto collectionSheetLoan;

    @Mock
    private CollectionSheetFormEnteredDataDto formEnteredDataDto;
    
    private UserContext userContext;
    private BulkEntryActionForm collectionSheetForm;
    private static final Short defaultCurrencyId = Short.valueOf("2");
    
    @BeforeClass
    public static void setupMifosLoggerDueToUseOfStaticClientRules() {
         MifosLogManager.configureLogging();
         MifosCurrency defaultCurrency = new MifosCurrency(defaultCurrencyId, null, null, null, null, null, null, null);
         Money.setDefaultCurrency(defaultCurrency);
    }

    @Before
    public void setupSUTAndInjectMocksAsDependencies() {
        
        userContext = new UserContext();
        userContext.setBranchId(Short.valueOf("1"));
        userContext.setId(Short.valueOf("1"));
        userContext.setLevel(PersonnelLevel.LOAN_OFFICER);
        
        collectionSheetForm = new BulkEntryActionForm();
        collectionSheetForm.setOfficeId("2");
        collectionSheetForm.setLoanOfficerId("2");
        collectionSheetForm.setCustomerId("2");
        collectionSheetForm.setPaymentId("2");
        
        collectionSheetServiceFacadeWebTier = new CollectionSheetServiceFacadeWebTier(officePersistence,
                masterPersistence, personnelPersistence, customerPersistence, collectionSheetService,
                collectionSheetGridViewAssembler, clientAttendanceAssembler,
                loanAccountAssembler,
                customerAccountAssembler, savingsAccountAssembler, accountPaymentAssembler);
    }
    
    @Test
    public void shouldConvertMasterDataEntitiesToListItemsAndPopulateDtoWithPaymentTypes() throws Exception {

        // setup
        final Short paymentTypeId = Short.valueOf("2");
        final String paymentTypeName = "type1";
        final ListItem<Short> paymentType1 = new ListItem<Short>(paymentTypeId, paymentTypeName);
        
        final List<MasterDataEntity> paymentTypeEntities = new ArrayList<MasterDataEntity>();
        paymentTypeEntities.add(masterDataEntity);

        // stubbing
        when(masterDataEntity.getId()).thenReturn(paymentTypeId);
        when(masterDataEntity.getName()).thenReturn(paymentTypeName);
        when(masterPersistence.retrieveMasterEntities(PaymentTypeEntity.class, Short.valueOf("1"))).thenReturn(
                paymentTypeEntities);

        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier
                .loadAllActiveBranchesAndSubsequentDataIfApplicable(userContext);

        // verification
        assertThat(formDto.getPaymentTypesList(), hasItem(paymentType1));
    }
    
    @Test
    public void shouldPopulateDtoWithActiveBranches() throws Exception {

        // setup
        final Short branchId = userContext.getBranchId();
        final Short branchId2 = Short.valueOf("2");
        final Short levelId = OfficeLevel.BRANCHOFFICE.getValue();
        
        // we typcially don't try to mock/stub value objects (DTO) as they have
        // no behaviour so just use as you would in production code.
        OfficeView officeStub1 = new OfficeView(branchId, "branchName1", levelId, Integer.valueOf(1));
        OfficeView officeStub2 = new OfficeView(branchId2, "branchName2", levelId, Integer.valueOf(1));
        List<OfficeView> activeOffices = Arrays.asList(officeStub1, officeStub2);

        // stub interaction with DAO/Persistence layer.
        when(officePersistence.getActiveOffices(branchId)).thenReturn(activeOffices);
        
        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier
                .loadAllActiveBranchesAndSubsequentDataIfApplicable(userContext);

        // verification
        assertThat(formDto.getActiveBranchesList(), is(activeOffices));
    }
    
    @Test
    public void shouldPopulateDtoWithLoanOfficersWhenOnlyOneActiveBranchExists() throws Exception {

        // setup
        final Short branchId = userContext.getBranchId();
        final Short levelId = OfficeLevel.BRANCHOFFICE.getValue();
        OfficeView officeStub1 = new OfficeView(branchId, "branchName1", levelId, Integer.valueOf(1));
        List<OfficeView> onlyOneActiveBranch = Arrays.asList(officeStub1);
        
        final PersonnelView loanOfficer1 = new PersonnelView(Short.valueOf("1"), "LoanOfficer1");
        final PersonnelView loanOfficer2 = new PersonnelView(Short.valueOf("2"), "LoanOfficer2");
        List<PersonnelView> loanOfficers = Arrays.asList(loanOfficer1, loanOfficer2);
        
        // stub interaction with DAO/Persistence layer.
        when(officePersistence.getActiveOffices(branchId)).thenReturn(onlyOneActiveBranch);
        when(
                personnelPersistence.getActiveLoanOfficersInBranch(PersonnelConstants.LOAN_OFFICER, branchId,
                        userContext.getId(), userContext.getLevelId())).thenReturn(loanOfficers);

        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier
                .loadAllActiveBranchesAndSubsequentDataIfApplicable(userContext);

        // verification
        assertThat(formDto.getLoanOfficerList(), is(loanOfficers));
    }
    
    @Test
    public void shouldPopulateDtoWithCustomersWhenOnlyOneActiveBranchAndOneLoanOfficerExists() throws Exception {

        // setup
        final Short branchId = userContext.getBranchId();
        final Short levelId = OfficeLevel.BRANCHOFFICE.getValue();
        OfficeView officeStub1 = new OfficeView(branchId, "branchName1", levelId, Integer.valueOf(1));
        List<OfficeView> onlyOneActiveBranch = Arrays.asList(officeStub1);

        final PersonnelView loanOfficer1 = new PersonnelView(Short.valueOf("1"), "LoanOfficer1");
        List<PersonnelView> onlyOneActiveLoanOfficer = Arrays.asList(loanOfficer1);
        
        final CustomerView customer1 = new CustomerView();
        List<CustomerView> customers = Arrays.asList(customer1);

        // stub interaction with DAO/Persistence layer.
        when(officePersistence.getActiveOffices(branchId)).thenReturn(onlyOneActiveBranch);
        when(
                personnelPersistence.getActiveLoanOfficersInBranch(PersonnelConstants.LOAN_OFFICER, branchId,
                        userContext.getId(), userContext.getLevelId())).thenReturn(onlyOneActiveLoanOfficer);
        when(customerPersistence.getActiveParentList(loanOfficer1.getPersonnelId(), CustomerLevel.CENTER.getValue(),
                branchId)).thenReturn(customers);

        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier
                .loadAllActiveBranchesAndSubsequentDataIfApplicable(userContext);

        // verification
        assertThat(formDto.getCustomerList(), is(customers));
    }
    
    @Test
    public void shouldPopulateDtoWithValueToForceFormNotToBeRefreshedWhenAllDropdownListDataIsFetched()
            throws Exception {

        // setup
        final Short branchId = userContext.getBranchId();
        final Short levelId = OfficeLevel.BRANCHOFFICE.getValue();
        OfficeView officeStub1 = new OfficeView(branchId, "branchName1", levelId, Integer.valueOf(1));
        List<OfficeView> onlyOneActiveBranch = Arrays.asList(officeStub1);

        final PersonnelView loanOfficer1 = new PersonnelView(Short.valueOf("1"), "LoanOfficer1");
        List<PersonnelView> onlyOneActiveLoanOfficer = Arrays.asList(loanOfficer1);

        final CustomerView customer1 = new CustomerView();
        List<CustomerView> customers = Arrays.asList(customer1);

        // stub interaction with DAO/Persistence layer.
        when(officePersistence.getActiveOffices(branchId)).thenReturn(onlyOneActiveBranch);
        when(
                personnelPersistence.getActiveLoanOfficersInBranch(PersonnelConstants.LOAN_OFFICER, branchId,
                        userContext.getId(), userContext.getLevelId())).thenReturn(onlyOneActiveLoanOfficer);
        when(
                customerPersistence.getActiveParentList(loanOfficer1.getPersonnelId(), CustomerLevel.CENTER.getValue(),
                        branchId)).thenReturn(customers);

        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier
                .loadAllActiveBranchesAndSubsequentDataIfApplicable(userContext);

        // verification
        assertThat(formDto.getReloadFormAutomatically(), is(Constants.NO));
    }
    
    @Test
    public void shouldPopulateDtoWithValueToForceFormToRefreshedWhenMoreDropdownDataNeedsToBeFetched() throws Exception {

        // setup
        final Short branchId = userContext.getBranchId();
        final Short branchId2 = Short.valueOf("2");
        final Short levelId = OfficeLevel.BRANCHOFFICE.getValue();

        OfficeView officeStub1 = new OfficeView(branchId, "branchName1", levelId, Integer.valueOf(1));
        OfficeView officeStub2 = new OfficeView(branchId2, "branchName2", levelId, Integer.valueOf(1));
        List<OfficeView> activeOffices = Arrays.asList(officeStub1, officeStub2);

        // stub interaction with DAO/Persistence layer.
        when(officePersistence.getActiveOffices(branchId)).thenReturn(activeOffices);

        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier
                .loadAllActiveBranchesAndSubsequentDataIfApplicable(userContext);

        // verification
        assertThat(formDto.getReloadFormAutomatically(), is(Constants.YES));
    }
    
    @Test
    public void shouldPopulateDtoWithValueToDisallowBackDatedTransactions() throws Exception {

        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier
                .loadAllActiveBranchesAndSubsequentDataIfApplicable(userContext);

        // verification
        assertThat(formDto.getBackDatedTransactionAllowed(), is(Constants.NO));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void shouldPopulateDtoWithLoanOfficersAndPreviouslyPopulatedDataWithDto() throws Exception {

        // setup
        final Short branchId = userContext.getBranchId();
        final Short levelId = OfficeLevel.BRANCHOFFICE.getValue();
        
        final OfficeView officeStub1 = new OfficeView(branchId, "branchName1", levelId, Integer.valueOf(1));
        final List<OfficeView> onlyOneActiveBranch = Arrays.asList(officeStub1);
        
        final ListItem<Short> paymentType1 = new ListItem<Short>(Short.valueOf("1"), "paymentType1");
        
        final CollectionSheetEntryFormDto previousCollectionSheetFormDto = new CollectionSheetEntryFormDto(
                onlyOneActiveBranch,
                Arrays
                .<ListItem<Short>> asList(paymentType1), new ArrayList<PersonnelView>(),
                new ArrayList<CustomerView>(), Constants.YES,
                Constants.YES, Constants.YES);

        final PersonnelView loanOfficer1 = new PersonnelView(Short.valueOf("1"), "LoanOfficer1");
        final PersonnelView loanOfficer2 = new PersonnelView(Short.valueOf("2"), "LoanOfficer2");
        final List<PersonnelView> loanOfficers = Arrays.asList(loanOfficer1, loanOfficer2);

        // stub interaction with DAO/Persistence layer.
        when(
                personnelPersistence.getActiveLoanOfficersInBranch(PersonnelConstants.LOAN_OFFICER, branchId,
                        userContext.getId(), userContext.getLevelId())).thenReturn(loanOfficers);

        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier.loadLoanOfficersForBranch(branchId,
                userContext, previousCollectionSheetFormDto);

        // verification
        assertThat(formDto.getLoanOfficerList(), is(loanOfficers));
        
        // assert rest of data comes from previousDto
        assertThat(formDto.getActiveBranchesList(), is(previousCollectionSheetFormDto.getActiveBranchesList()));
    }
    
    @Test
    public void shouldPopulateDtoWithLatestMeetingDateWhenBackDatedTransactionsAreAllowed() throws Exception {

        // setup
        final Integer customerId = Integer.valueOf(3);
        final CustomerView customer1 = new CustomerView();
        customer1.setCustomerId(customerId);
        final List<CustomerView> customers = Arrays.asList(customer1);
        
        final Date expectedMeetingDateAsJavaDate = new DateTime().plusDays(2).toDate();
        final java.sql.Date expectedMeetingDateAsSqlDate = new java.sql.Date(expectedMeetingDateAsJavaDate.getTime());

        final CollectionSheetEntryFormDto previousCollectionSheetFormDto = new CollectionSheetEntryFormDto(
                new ArrayList<OfficeView>(),
                new ArrayList<ListItem<Short>>(), new ArrayList<PersonnelView>(),
                customers,
                Constants.YES, Constants.YES, Constants.YES);

        when(customerPersistence.getLastMeetingDateForCustomer(customerId)).thenReturn(expectedMeetingDateAsSqlDate);
        
        // NOTE: Backdated transactions is checked through static
        // AccountingRules so can't set up for now
        
        // exercise test
        CollectionSheetEntryFormDto formDto = collectionSheetServiceFacadeWebTier.loadMeetingDateForCustomer(
                customerId,
                previousCollectionSheetFormDto);

        // verification
        assertThat(formDto.getMeetingDate(), is(expectedMeetingDateAsJavaDate));

        // assert rest of data comes from previousDto
        assertThat(formDto.getCustomerList(), is(customers));
    }
    
    @Test
    public void shouldTranslateCollectionSheetDataIntoCollectionSheetEntryGridDtoType() throws Exception {

        // setup
        final List<CollectionSheetCustomerDto> collectionSheetCustomer = Arrays.asList(centerCustomer);
        
        final PersonnelView loanOfficer = new PersonnelView(Short.valueOf("1"), "loanOfficer");
        final OfficeView office = new OfficeView(Short.valueOf("1"), "office", OfficeLevel.BRANCHOFFICE,
                "levelNameKey", Integer.valueOf(1));
        final Short paymentTypeId = Short.valueOf("99");
        final ListItem<Short> paymentType = new ListItem<Short>(paymentTypeId, "item1");
        final Date today = new DateTime().toDate();
        final String receiptNumber = "XXX-120";
        
        final Integer accountId = Integer.valueOf("55");
        final Short currencyId = Short.valueOf("2");
        final Double totalCustomerAccountCollectionFee = Double.valueOf("29.87");
        final CollectionSheetCustomerAccountDto customerAccountDto = new CollectionSheetCustomerAccountDto(accountId,
                currencyId,
                totalCustomerAccountCollectionFee);
        
        // stubbing
        when(collectionSheetData.getCollectionSheetCustomer()).thenReturn(collectionSheetCustomer);
        when(centerCustomer.getCollectionSheetCustomerAccount()).thenReturn(customerAccountDto);
        
        
        when(formEnteredDataDto.getLoanOfficer()).thenReturn(loanOfficer);
        when(formEnteredDataDto.getOffice()).thenReturn(office);
        when(formEnteredDataDto.getPaymentType()).thenReturn(paymentType);
        when(formEnteredDataDto.getMeetingDate()).thenReturn(today);
        when(formEnteredDataDto.getReceiptDate()).thenReturn(today);
        when(formEnteredDataDto.getReceiptId()).thenReturn(receiptNumber);
        
        // exercise test
        final CollectionSheetEntryGridDto formDto = collectionSheetServiceFacadeWebTier.translate(collectionSheetData,
                formEnteredDataDto, null);

        // verification
        assertThat(formDto.getTotalCustomers(), is(collectionSheetCustomer.size()));
        
        // assert that the parent entry view exists with correctly populated
        
        // assert form selected data set on dto
        assertThat(formDto.getLoanOfficer(), is(loanOfficer));
        assertThat(formDto.getOffice(), is(office));
        assertThat(formDto.getPaymentType(), is(paymentType));
        assertThat(formDto.getTransactionDate(), is(today));
        assertThat(formDto.getReceiptDate(), is(today));
        assertThat(formDto.getReceiptId(), is(receiptNumber));
        assertThat(formDto.getPaymentTypeId(), is(paymentTypeId));
    }
    
    @Test
    public void shouldTranslateCollectionSheetDataXXX() throws Exception {

        // setup
        final Integer customerId = Integer.valueOf(7);
        
        final Integer customerAccountId = Integer.valueOf("55");
        final Integer savingsAccountId = Integer.valueOf("77");
        final String savingsProductShortName = "sav1";
        final Double savingsDepositDue = Double.valueOf("125.00");
        
        final Integer loanAccountId = Integer.valueOf("101");
        final String loanProductShortName = "lon1";
        final Double totalRepaymentDue = Double.valueOf("67.00");
        
        final Double totalCustomerAccountCollectionFee = Double.valueOf("29.87");
        final CollectionSheetCustomerAccountDto customerAccountDto = new CollectionSheetCustomerAccountDto(
                customerAccountId,
                defaultCurrencyId,
                totalCustomerAccountCollectionFee);
        
        final List<CollectionSheetCustomerDto> collectionSheetCustomer = Arrays.asList(centerCustomer);
        final List<CollectionSheetCustomerSavingDto> collectionSheetCustomerSavings = Arrays
                .asList(collectionSheetSaving);
        final List<CollectionSheetCustomerLoanDto> collectionSheetCustomerLoans = Arrays.asList(collectionSheetLoan);

        // stubbing
        when(collectionSheetData.getCollectionSheetCustomer()).thenReturn(collectionSheetCustomer);
        
        when(centerCustomer.getCustomerId()).thenReturn(customerId);
        when(centerCustomer.getCollectionSheetCustomerAccount()).thenReturn(customerAccountDto);
        when(centerCustomer.getCollectionSheetCustomerSaving()).thenReturn(collectionSheetCustomerSavings);
        when(centerCustomer.getCollectionSheetCustomerLoan()).thenReturn(collectionSheetCustomerLoans);
        
        // stub savings
        when(collectionSheetSaving.getCustomerId()).thenReturn(customerId);
        when(collectionSheetSaving.getAccountId()).thenReturn(savingsAccountId);
        when(collectionSheetSaving.getProductShortName()).thenReturn(savingsProductShortName);
        when(collectionSheetSaving.getTotalDepositAmount()).thenReturn(savingsDepositDue);
        
        // stub loans
        when(collectionSheetLoan.getCustomerId()).thenReturn(customerId);
        when(collectionSheetLoan.getAccountId()).thenReturn(loanAccountId);
        when(collectionSheetLoan.getProductShortName()).thenReturn(loanProductShortName);
        when(collectionSheetLoan.getPayInterestAtDisbursement()).thenReturn(Constants.NO);
        when(collectionSheetLoan.getTotalRepaymentDue()).thenReturn(totalRepaymentDue);
        when(collectionSheetLoan.getAccountStateId()).thenReturn(AccountState.LOAN_ACTIVE_IN_GOOD_STANDING.getValue());
        
        // exercise test
        final CollectionSheetEntryGridDto formDto = collectionSheetServiceFacadeWebTier.translate(collectionSheetData,
                formEnteredDataDto, null);

        // verification
        assertThat(formDto.getTotalCustomers(), is(collectionSheetCustomer.size()));

        // assert that the parent entry view exists with correctly populated
        final CollectionSheetEntryView collectionSheetEntryParent = formDto.getBulkEntryParent();
        assertThat(collectionSheetEntryParent, is(notNullValue()));
        
        assertThat(collectionSheetEntryParent.getAttendence(), is(nullValue()));
        assertThat(collectionSheetEntryParent.getCountOfCustomers(), is(1));
        assertThat(collectionSheetEntryParent.getCustomerDetail().getCustomerId(), is(customerId));
        
        // customer account details
        assertThat(collectionSheetEntryParent.getCustomerAccountDetails().getAccountId(), is(customerAccountId));
        assertThat(collectionSheetEntryParent.getCustomerAccountDetails().getTotalAmountDue().getAmountDoubleValue(),
                is(totalCustomerAccountCollectionFee));
        
        // savings account
        List<SavingsAccountView> savingAccounts = collectionSheetEntryParent.getSavingsAccountDetails();
        assertThat(savingAccounts.size(), is(1));
        assertThat(savingAccounts.get(0).getAccountId(), is(savingsAccountId));
        assertThat(savingAccounts.get(0).getCustomerId(), is(customerId));
        assertThat(savingAccounts.get(0).getTotalDepositDue(), is(savingsDepositDue));
        
        // loan accounts
        List<LoanAccountsProductView> loanAccountProductViews = collectionSheetEntryParent.getLoanAccountDetails();
        assertThat(loanAccountProductViews.size(), is(1));
        assertThat(loanAccountProductViews.get(0).getPrdOfferingShortName(), is(loanProductShortName));
        assertThat(loanAccountProductViews.get(0).getTotalAmountDue(), is(totalRepaymentDue));
        
        assertThat(loanAccountProductViews.get(0).getLoanAccountViews().size(), is(1));
        assertThat(loanAccountProductViews.get(0).getLoanAccountViews().get(0).getTotalAmountDue(),
                is(totalRepaymentDue));
    }
}