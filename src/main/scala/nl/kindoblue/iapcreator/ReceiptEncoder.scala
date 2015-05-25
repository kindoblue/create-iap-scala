package nl.kindoblue.iapcreator

import org.bouncycastle.asn1._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen


/**
 * Created by Stefano on 25/05/15.
 */


object ReceiptEncoder {


  private def encodeField(fieldType: Int, version: Int, value: DEROctetString) : DLSequence = {
    val arr : Array[ASN1Encodable] = Array(
      new ASN1Integer(fieldType),
      new ASN1Integer(version),
      value)

    new DLSequence(arr)

  }

  private def encodeBytes(fieldType: Int, version: Int, value: Array[Byte]) : DLSequence = {

    encodeField(fieldType, version, new DEROctetString(value))

  }

  private def encodeStringField(fieldType: Int, version: Int, value: String) : DLSequence = {

    val der = new DERUTF8String(value)

    encodeField(fieldType, version, new DEROctetString(der.getEncoded))

  }

  private def encodeIntegerField(fieldType: Int, version: Int, value: Int) : DLSequence = {

    val der = new ASN1Integer(value)

    encodeField(fieldType, version, new DEROctetString(der.getEncoded))

  }

  private def encodeDateField(fieldType: Int, version: Int, value: Option[DateTime]) : DLSequence = {

    val fmt = ISODateTimeFormat.dateTime()

    val strDate = value match {
      case Some(date) => fmt.print(date)
      case _ => ""
    }

    val der = new DERIA5String(strDate)

    encodeField(fieldType, version, new DEROctetString(der.getEncoded))

  }

  private def encodePurchase(purchase: Purchase) : DLSequence = {

    // subscription date, for the moment always empty
    val subcriptionEnd = encodeDateField(1708, 1, None)

    // add some anonymous fields, we know that are there, not used
    val anon1 = encodeStringField(1709, 1, "")
    val anon2 = encodeStringField(1714, 1, "")
    val anon3 = encodeStringField(1715, 1, "")
    val anon4 = encodeStringField(1716, 1, "")
    val anon5 = encodeStringField(1717, 1, "")
    val anon6 = encodeStringField(1718, 1, "")

    // cancellation date
    val cancellation = encodeDateField(1712, 1, purchase.cancellationDate)

    // quantity
    val quantity = encodeIntegerField(1701, 1, purchase.quantity)
    // add some anonymous integer fields
    val anon7 = encodeIntegerField(1707, 1, 1)
    val anon8 = encodeIntegerField(1711, 1, 1)
    val anon9 = encodeIntegerField(1713, 1, 1)
    val anon10 = encodeIntegerField(1710, 1, 391086247)

    // add transaction id
    val transactionID = encodeStringField(1703, 1, purchase.transactionID)
    // add original transaction id
    val orgTransactionID = encodeStringField(1705, 1, purchase.orgTransactionID)
    // add purchase date
    val purchaseDate = encodeDateField(1704, 1, Some(purchase.purchaseDate))

    // add original purchase date
    val orgPurchaseDate = encodeDateField(1706, 1, Some(purchase.orgPurchaseDate))

    // add product id
    val productID = encodeStringField(1702, 1, purchase.productID)

    // create an array with all the fields, to be packed in a DLSet
    val arr : Array[ASN1Encodable] = Array(
      subcriptionEnd,
      anon1,
      anon2,
      anon3,
      anon4,
      anon5,
      anon6,
      quantity,
      anon7,
      anon8,
      anon9,
      anon10,
      cancellation,
      transactionID,
      orgTransactionID,
      purchaseDate,
      orgPurchaseDate,
      productID)

    // create the DLSet
    val dlset = new DLSet(arr)

    // finally encode the purchase record as a DLSequence
    encodeField(17, 1, new DEROctetString(dlset.getEncoded))

  }


  def encode(receipt: Receipt, vendorID: String) : Array[Byte] = {

    import ReceiptType._

    // production, sandbox, custom (invented by me)
    val str = receipt.receiptType match {
      case Production => "Production"
      case ProductionSandbox => "ProductionSandbox"
      case CustomSandbox => "CustomSandbox"
    }
    val receiptType = encodeStringField(0, 1, str)

    // bundle identifier
    val bundleID =  encodeStringField(2, 1, receipt.bundleID)

    // creation date??
    val creationDate = encodeDateField(12, 1, Some(receipt.creationDate))

    // other dates
    val date1 = encodeDateField(8, 1, Some(receipt.creationDate.minusHours(1)))
    val date2 = encodeDateField(18, 1, Some(receipt.creationDate.minusHours(2)))

    // original application version
    val orgVer =  encodeStringField(19, 1, receipt.originalAppVersion.getOrElse(""))

    // anon empty string
    val emptyStr = encodeStringField(20, 1, "")

    // series of anon integers, we just pick some random numbers
    val anon1 = encodeIntegerField(14, 1, Gen.choose(0,106).sample.getOrElse(0))
    val anon2 = encodeIntegerField(11, 1, Gen.choose(0,100000).sample.getOrElse(0))
    val anon3 = encodeIntegerField(13, 1, Gen.oneOf(70102,80102).sample.getOrElse(0))
    val anon4 = encodeIntegerField(1, 1, Gen.choose(0,100000).sample.getOrElse(0))
    val anon5 = encodeIntegerField(9, 1, Gen.choose(0,100000).sample.getOrElse(0))
    val anon6 = encodeIntegerField(16, 1, Gen.choose(0,100000).sample.getOrElse(0))
    val anon7 = encodeIntegerField(25, 1, 3)
    val anon8 = encodeIntegerField(15, 1, Gen.choose(0,830681071).sample.getOrElse(0))

    // generate random byte array for field 6,7
    val genBytes =for {
      n <- Gen.choose(50,80)
      y <- Gen.containerOfN[Array, Byte](n, arbitrary[Byte])
    } yield y

    val anon9 = encodeBytes(6, 1, genBytes.sample.getOrElse(Array()))
    val anon10 = encodeBytes(7, 1, genBytes.sample.getOrElse(Array()))


    // create an array with all the fields, to be packed in a DLSet
    val lst : List[ASN1Encodable] = List(
      receiptType,
      bundleID,
      creationDate,
      date1,
      date2,
      orgVer,
      emptyStr,
      anon1,
      anon2,
      anon3,
      anon4,
      anon5,
      anon6,
      anon7,
      anon8,
      anon9,
      anon10)


    // append the purchases
    val complete : List[ASN1Encodable] = lst ::: receipt.purchases.map(encodePurchase)

    // create the DLSet
    val dlset = new DLSet(complete.toArray)

    // calculate sha-1 based on opaque value (chosed randomly) the vendor id and bundle id

    // return
    dlset.getEncoded

  }

}