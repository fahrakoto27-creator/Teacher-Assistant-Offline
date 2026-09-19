package com.fahendrena.teacherassistant.pdf

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File

/**
 * Extraction de texte 100% locale (aucun appel reseau) pour indexer
 * les PDF importes par l'enseignant et permettre la recherche locale.
 */
object PdfTextExtractor {
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            PDFBoxResourceLoader.init(context.applicationContext)
            initialized = true
        }
    }

    fun extractText(context: Context, file: File): String {
        init(context)
        val document = PDDocument.load(file)
        return try {
            PDFTextStripper().getText(document)
        } finally {
            document.close()
        }
    }

    /**
     * Extrait le texte page par page (index 0 = page 1) afin de pouvoir
     * citer precisement la source (document + numero de page) pour toute
     * information reutilisee dans une fiche de preparation.
     */
    fun extractPages(context: Context, file: File): List<String> {
        init(context)
        val document = PDDocument.load(file)
        return try {
            val stripper = PDFTextStripper()
            val pageCount = document.numberOfPages
            (1..pageCount).map { pageIndex ->
                stripper.startPage = pageIndex
                stripper.endPage = pageIndex
                stripper.getText(document)
            }
        } finally {
            document.close()
        }
    }
}
