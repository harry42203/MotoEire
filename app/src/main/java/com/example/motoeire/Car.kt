package com.example.motoeire
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nickname: String,
    
    @ColumnInfo(name = "registration_number")
    val registrationNumber: String,

    @ColumnInfo(name = "insurance_provider")
    val insuranceProvider: String,

    @ColumnInfo(name = "insurance_policy_number")
    val insurancePolicyNumber: String,

    // Storing dates as Long (Unix timestamps) is recommended for easier sorting/comparisons
    @ColumnInfo(name = "nct_renewal_date")
    val nctRenewalDate: Long,

    @ColumnInfo(name = "motor_tax_renewal_date")
    val motorTaxRenewalDate: Long
)