package uk.gov.hmrc.economiccrimelevyaccount.base

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlPathMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import uk.gov.hmrc.economiccrimelevyaccount.base.WireMockHelper.stub
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.FinancialDataResponse

trait IntegrationFrameworkStubs { self: WireMockStubs =>

  var validFinancialDetailsResponse =
    """
      |{
      |  "getFinancialData": {
      |    "financialDetails": {
      |      "totalisation": {
      |        "regimeTotalisation": {
      |          "totalAccountOverdue": 1000,
      |          "totalAccountNotYetDue": 250,
      |          "totalAccountCredit": 0,
      |          "totalAccountBalance": 1250
      |        },
      |        "targetedSearch_SelectionCriteriaTotalisation": {
      |          "totalOverdue": 100,
      |          "totalNotYetDue": 0,
      |          "totalBalance": 100,
      |          "totalCredit": 0,
      |          "totalCleared": 50
      |        }
      |      },
      |      "documentDetails": [
      |        {
      |          "documentNumber": "187346702498",
      |          "documentType": "TRM New Charge",
      |          "chargeReferenceNumber": "XP001286394838",
      |          "businessPartnerNumber": "100893731",
      |          "contractAccountNumber": "900726630",
      |          "contractAccountCategory": "ECL",
      |          "contractObjectNumber": "104920928302302",
      |          "contractObjectType": "ECL",
      |          "postingDate": "2022-01-01",
      |          "issueDate": "2022-01-01",
      |          "documentTotalAmount": 100,
      |          "documentClearedAmount": 100,
      |          "documentOutstandingAmount": 0,
      |          "documentLockDetails": {
      |            "lockType": "Payment",
      |            "lockStartDate": "2022-01-01",
      |            "lockEndDate": "2022-01-01"
      |          },
      |          "documentInterestTotals": {
      |            "interestPostedAmount": 13.12,
      |            "interestPostedChargeRef": "XB001286323438",
      |            "interestAccruingAmount": 12.1
      |          },
      |          "documentPenaltyTotals": [
      |            {
      |              "penaltyType": "LPP1",
      |              "penaltyStatus": "POSTED",
      |              "penaltyAmount": 10.01,
      |              "postedChargeReference": "XR00123933492"
      |            }
      |          ],
      |          "lineItemDetails": [
      |            {
      |              "itemNumber": "0001",
      |              "subItemNumber": "003",
      |              "mainTransaction": "6220",
      |              "subTransaction": "3410",
      |              "chargeDescription": "ECL Return",
      |              "periodFromDate": "2022-01-01",
      |              "periodToDate": "2022-01-31",
      |              "periodKey": "22YD",
      |              "netDueDate": "2022-02-08",
      |              "formBundleNumber": "125435934761",
      |              "statisticalKey": "1",
      |              "amount": 3420,
      |              "clearingDate": "2022-02-09",
      |              "clearingReason": "01",
      |              "clearingDocument": "719283701921",
      |              "outgoingPaymentMethod": "B",
      |              "ddCollectionInProgress": true,
      |              "lineItemLockDetails": [
      |                {
      |                  "lockType": "Payment",
      |                  "lockStartDate": "2022-01-01",
      |                  "lockEndDate": "2022-01-01"
      |                }
      |              ]
      |            }
      |          ]
      |        }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin

  def stubGetFinancialDetails(financialDetails: FinancialDataResponse): StubMapping =
    stub(
      get(
        urlPathMatching("^/penalty/financial-data/ZECL/.*")
      ),
      aResponse()
        .withStatus(OK)
        .withBody(validFinancialDetailsResponse)
    )

}
