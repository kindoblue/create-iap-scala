package nl.kindoblue.iapcreator


import org.joda.time.DateTime

/**
 * Created by Stefano on 25/05/15.
 */

object ReceiptType extends Enumeration {
  type ReceiptType = Value
  val Production, ProductionSandbox, CustomSandbox = Value
}


case class Purchase(quantity: Int,
                    productID: String,
                    transactionID: String,
                    orgTransactionID: String,
                    purchaseDate: DateTime,
                    orgPurchaseDate: DateTime,
                    cancellationDate: Option[DateTime] = None)

import ReceiptType._
case class Receipt(receiptType: ReceiptType,
                   bundleID: String,
                   appVersion: String,
                   purchases: List[Purchase],
                   originalAppVersion: Option[String] = None,
                   ageRating: Option[String] = None) {

  val creationDate: DateTime = new DateTime()

}