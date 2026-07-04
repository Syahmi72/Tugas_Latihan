// DataModels.kt
// Modul Pemrograman Berbasis Objek - TE14027
// PBL Sprint - M5 : Penerapan Data Model & Enkapsulasi
//
// Catatan Review UML:
// - Di Class Diagram minggu lalu, atribut "isTersedia" pada KamarKos masih
//   ditandai '+' (public). Ini melanggar prinsip enkapsulasi karena status
//   ketersediaan kamar bisa diubah dari luar class tanpa lewat validasi.
//   -> Sudah diperbaiki di sini menjadi 'private set', hanya bisa diubah
//      lewat method ubahStatusKamar().

import java.util.regex.Pattern

/**
 * Kelas dasar (superclass) untuk semua pengguna sistem.
 * Seluruh atribut private, hanya bisa dibaca dari luar (private set),
 * kecuali email yang punya custom setter dengan validasi format.
 */
open class Pengguna(
    userId: String,
    nama: String,
    email: String
) {
    var userId: String = userId
        private set

    var nama: String = nama
        private set

    // Enkapsulasi + Validasi: format email harus valid
    var email: String = email
        set(value) {
            require(isEmailValid(value)) { "Format email tidak valid: $value" }
            field = value
        }

    init {
        require(isEmailValid(email)) { "Format email tidak valid: $email" }
    }

    private fun isEmailValid(value: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)\\.[A-Za-z]{2,}$")
        return pattern.matcher(value).matches()
    }

    open fun login(): Boolean {
        println("$nama berhasil login dengan email $email")
        return true
    }
}

/**
 * PencariKos - turunan Pengguna, mencari dan memesan kamar.
 */
class PencariKos(
    userId: String,
    nama: String,
    email: String
) : Pengguna(userId, nama, email) {

    // Private penuh, tidak diekspos langsung sebagai MutableList ke luar
    private val riwayatSewa: MutableList<Transaksi> = mutableListOf()

    fun getRiwayatSewa(): List<Transaksi> = riwayatSewa.toList()

    fun cariKamar(daftarKamar: List<KamarKos>): List<KamarKos> {
        return daftarKamar.filter { it.isTersedia }
    }

    fun pesanKamar(kamar: KamarKos): Boolean {
        if (!kamar.isTersedia) {
            println("Kamar ${kamar.nomorKamar} sudah tidak tersedia.")
            return false
        }
        val transaksiBaru = Transaksi(
            transaksiId = "TRX-${System.currentTimeMillis()}",
            kamar = kamar,
            totalHarga = kamar.harga
        )
        riwayatSewa.add(transaksiBaru)
        kamar.ubahStatusKamar(false)
        println("Kamar ${kamar.nomorKamar} berhasil dipesan oleh $nama.")
        return true
    }
}

/**
 * PemilikKos - turunan Pengguna, mengelola daftar kamar.
 */
class PemilikKos(
    userId: String,
    nama: String,
    email: String
) : Pengguna(userId, nama, email) {

    private val daftarKamar: MutableList<KamarKos> = mutableListOf()

    fun getDaftarKamar(): List<KamarKos> = daftarKamar.toList()

    fun tambahKamar(kamar: KamarKos) {
        daftarKamar.add(kamar)
        println("Kamar ${kamar.nomorKamar} ditambahkan.")
    }

    fun ubahStatusKamar(nomorKamar: String, statusBaru: Boolean): Boolean {
        val kamar = daftarKamar.find { it.nomorKamar == nomorKamar } ?: return false
        return kamar.ubahStatusKamar(statusBaru)
    }
}

/**
 * KamarKos - merepresentasikan satu unit kamar kos.
 * Enkapsulasi: isTersedia diperbaiki dari public jadi private set.
 * Validasi: harga tidak boleh minus atau nol.
 */
class KamarKos(
    nomorKamar: String,
    harga: Int
) {
    var nomorKamar: String = nomorKamar
        private set

    // Enkapsulasi + Validasi: harga sewa tidak boleh minus/nol
    var harga: Int = harga
        set(value) {
            require(value > 0) { "Harga kamar tidak boleh minus atau nol: $value" }
            field = value
        }

    // Diperbaiki dari '+' (public) menjadi private set sesuai hasil review UML
    var isTersedia: Boolean = true
        private set

    init {
        require(harga > 0) { "Harga kamar tidak boleh minus atau nol: $harga" }
    }

    fun tampilkanInfo() {
        println("Kamar $nomorKamar | Harga: Rp$harga | Tersedia: $isTersedia")
    }

    fun ubahStatusKamar(statusBaru: Boolean): Boolean {
        isTersedia = statusBaru
        return true
    }
}

/**
 * Transaksi - mencatat proses sewa antara PencariKos dan KamarKos.
 * Validasi: totalHarga tidak boleh minus.
 */
class Transaksi(
    transaksiId: String,
    val kamar: KamarKos,
    totalHarga: Int
) {
    var transaksiId: String = transaksiId
        private set

    // Enkapsulasi + Validasi: totalHarga tidak boleh minus
    var totalHarga: Int = totalHarga
        set(value) {
            require(value >= 0) { "Total harga tidak boleh bernilai minus: $value" }
            field = value
        }

    var statusBayar: String = "BELUM_BAYAR"
        private set

    init {
        require(totalHarga >= 0) { "Total harga tidak boleh bernilai minus: $totalHarga" }
    }

    fun buatTransaksi(): Boolean {
        statusBayar = "LUNAS"
        println("Transaksi $transaksiId berhasil dibuat, status: $statusBayar")
        return true
    }

    fun batalkanTransaksi() {
        statusBayar = "DIBATALKAN"
        kamar.ubahStatusKamar(true)
        println("Transaksi $transaksiId dibatalkan, kamar ${kamar.nomorKamar} tersedia lagi.")
    }
}