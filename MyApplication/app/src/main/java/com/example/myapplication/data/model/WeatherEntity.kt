package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.R
import com.example.myapplication.data.database.Converters

@Entity(tableName = "weather_data")
@TypeConverters(Converters::class)
data class WeatherEntity(
    @PrimaryKey
    val cityId: String,
    val temperature: Double,
    val windSpeed: Double,
    val condition: String,
    val minTemp: Double,
    val maxTemp: Double,
    val timestamp: Long = System.currentTimeMillis(), // Ajout du timestamp
    val hourlyTemperatures: List<Double>,
    val hourlyTimes: List<String>
)



fun WeatherEntity.getWeatherIcon(): Int {
    return when {
        // Conditions de pluie
        condition.lowercase().contains("rain") -> {
            when {
                temperature < 5 -> R.drawable.pluie_legere  // Pluie froide
                temperature < 15 -> R.drawable.pluvieux     // Pluie modérée
                else -> R.drawable.averse                   // Pluie chaude
            }
        }

        // Conditions de ciel clair
        condition.lowercase().contains("clear") || condition.lowercase().contains("sunny") -> {
            when {
                temperature < 10 -> R.drawable.ensoleil     // Ensoleillé mais froid
                temperature < 25 -> R.drawable.ensoleil     // Ensoleillé tempéré
                else -> R.drawable.ensoleil                 // Très chaud et ensoleillé
            }
        }

        // Conditions nuageuses
        condition.lowercase().contains("cloud") -> {
            when {
                temperature < 10 -> R.drawable.brouillard   // Nuageux et froid
                temperature < 20 -> R.drawable.nuageux      // Nuageux tempéré
                else -> R.drawable.nuageux                  // Nuageux et chaud
            }
        }

        // Conditions de neige
        condition.lowercase().contains("snow") -> {
            when {
                temperature < 0 -> R.drawable.grele         // Neige forte
                else -> R.drawable.grele                    // Neige légère
            }
        }

        // Conditions d'orage
        condition.lowercase().contains("thunder") || condition.lowercase().contains("storm") -> {
            R.drawable.pluie_legere
        }

        // Condition par défaut
        else -> when {
            temperature < 0 -> R.drawable.grele            // Très froid
            temperature < 10 -> R.drawable.brouillard      // Froid
            temperature < 20 -> R.drawable.nuageux         // Tempéré
            temperature < 30 -> R.drawable.ensoleil        // Chaud
            else -> R.drawable.ensoleil                    // Très chaud
        }
    }
}
