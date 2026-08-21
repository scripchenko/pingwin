package ru.scripchenko.autovless

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.FrameLayout
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class QrScannerActivity : Activity() {

    private lateinit var captureManager: CaptureManager
    private lateinit var barcodeView: DecoratedBarcodeView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        val root =
            FrameLayout(this).apply {
                setBackgroundColor(
                    Color.BLACK
                )
            }

        barcodeView =
            DecoratedBarcodeView(this).apply {
                setStatusText(
                    getString(
                        R.string.qr_scanner_status
                    )
                )
            }

        val scannerSize =
            (260 * resources.displayMetrics.density)
                .toInt()

        val layoutParams =
            FrameLayout.LayoutParams(
                scannerSize,
                scannerSize
            ).apply {
                gravity =
                    Gravity.CENTER
            }

        root.addView(
            barcodeView,
            layoutParams
        )

        setContentView(root)

        captureManager =
            CaptureManager(
                this,
                barcodeView
            ).apply {
                initializeFromIntent(
                    intent,
                    savedInstanceState
                )
                setShowMissingCameraPermissionDialog(
                    true
                )
                decode()
            }
    }

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
    }

    override fun onPause() {
        captureManager.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        captureManager.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(
            outState
        )

        captureManager.onSaveInstanceState(
            outState
        )
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean =
        barcodeView.onKeyDown(
            keyCode,
            event
        ) ||
            super.onKeyDown(
                keyCode,
                event
            )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        captureManager.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }
}
