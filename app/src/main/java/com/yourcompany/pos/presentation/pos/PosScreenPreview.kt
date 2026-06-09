package com.yourcompany.pos.presentation.pos

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yourcompany.pos.presentation.theme.PosTheme

@Preview(name = "POS Expanded", widthDp = 1400, heightDp = 920, showBackground = true)
@Composable
private fun PosScreenPreviewExpanded() {
    PosTheme {
        PosScreen(
            state = PosPreviewData.sampleState(),
            onEvent = {}
        )
    }
}

@Preview(name = "POS Compact", widthDp = 420, heightDp = 920, showBackground = true)
@Composable
private fun PosScreenPreviewCompact() {
    PosTheme {
        PosScreen(
            state = PosPreviewData.sampleState(),
            onEvent = {}
        )
    }
}
