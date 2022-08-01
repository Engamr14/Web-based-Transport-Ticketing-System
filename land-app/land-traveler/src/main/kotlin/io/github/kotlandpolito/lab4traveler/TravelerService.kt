package io.github.kotlandpolito.lab4traveler

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

@Service
class TravelerService{

    @Value("\${server.jwt-key}")
    lateinit var jwtKey: String

    @Autowired
    var ticketRepo: TicketRepository?= null
    @Autowired
    var userDetailsRepo: UserRepository?= null
    @Autowired
    var transitRepo: TransitRepository?= null

    fun insertProfile(profile: UserDetails): Boolean{
        var resultInserted: UserDetails?= null
        resultInserted=userDetailsRepo?.save(profile)
        if (resultInserted != null) {
            return true
        }
        return false
    }

    fun getQRCode(ticketId: Long): ByteArray? {
        try {
            val qrCodeWriter = QRCodeWriter()
            val ticket = ticketRepo?.findById(ticketId)?.get()
            val qrContent= ticket?.jws
            val bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE,500, 500)
            val byteArrayOutputStream = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream)
            return byteArrayOutputStream.toByteArray()
        } catch (e: WriterException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getTransitsById(userId: String): Iterable<Transit>?{
        return transitRepo?.findByUserId(userId)
    }

    fun getTransits(): Iterable<Transit>?{
        return transitRepo?.findAll()
    }

    fun validateTicket(ticketJWS: String): Boolean{
        try {
            /* Try parsing the JWS, this includes checking its signature */
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtKey.toByteArray())).build().parseClaimsJws(ticketJWS).body
        }
        catch (e : JwtException) {
            /* If the validation fails, then the JWS was most likely invalid */
            e.printStackTrace()
            return false
        }

        /* Here it's safe to use the JWT */
        val referencedTicket = ticketRepo?.findByJws(ticketJWS)
        val newTransit = referencedTicket?.let { Transit(it.user, referencedTicket) }
        newTransit?.let { transitRepo?.save(it) }

        return true
    }

    fun validateTicket(qrCodeFile: File): Boolean{

        try {
            val bufferedImage:BufferedImage = ImageIO.read(qrCodeFile)
            val bufferedImageLuminanceSource = BufferedImageLuminanceSource(bufferedImage)
            val hybridBinarizer = HybridBinarizer(bufferedImageLuminanceSource)
            val binaryBitmap = BinaryBitmap(hybridBinarizer)
            val multiFormatReader = MultiFormatReader()

            val result: Result? = multiFormatReader.decode(binaryBitmap);
            val jwt: String? = result?.text;

            if(!jwt.equals(null)){
                val claims: Claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtKey.toByteArray())).build().parseClaimsJws(jwt).body
                val iat: String = claims["iat"] as String
                val exp: String = claims["exp"] as String

                val now=Date().time

                if (iat.toLong()+exp.toLong()>=now){
                    return false
                }
            }

            return true

        } catch (ex:Exception) {
            ex.printStackTrace()
        }
        return false
    }
}