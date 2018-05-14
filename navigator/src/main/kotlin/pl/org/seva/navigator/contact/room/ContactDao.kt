/*
 * Copyright (C) 2017 Wiktor Nizio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.org.seva.navigator.contact.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pl.org.seva.navigator.contact.Contact

infix fun ContactDao.insert(contact: Contact) = insertEntity(contact.toEntity())

infix fun ContactDao.delete(contact: Contact) = deleteEntity(contact.toEntity())

@Dao
interface ContactDao {

    @Query("select * from ${ContactsDatabase.TABLE_NAME}")
    fun getAll(): List<ContactEntity>

    @Insert
    fun insertEntity(contact: ContactEntity)

    @Delete
    fun deleteEntity(contact: ContactEntity)

    @Query("DELETE FROM ${ContactsDatabase.TABLE_NAME}")
    fun deleteAll(): Int
}
