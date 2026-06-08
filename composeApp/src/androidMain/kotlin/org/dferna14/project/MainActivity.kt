package org.dferna14.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dferna14.project.util.registrarActivityParaCierre
import org.dferna14.project.util.registrarContextoOcr
import org.dferna14.project.util.registrarContextoVoz

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        registrarActivityParaCierre(this)
        registrarContextoVoz(this)
        registrarContextoOcr(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
