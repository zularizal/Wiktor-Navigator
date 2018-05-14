package pl.org.seva.navigator.contact.room

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.org.seva.navigator.contact.Contact

@Entity(tableName = ContactsDatabase.TABLE_NAME)
class ContactEntity() {
    @PrimaryKey
    lateinit var email: String
    lateinit var name: String
    var color: Int = Color.GRAY

    constructor(contact: Contact) : this() {
        email = contact.email
        name = contact.name
        color = contact.color
    }

    fun value() = Contact(email, name, color)
}
