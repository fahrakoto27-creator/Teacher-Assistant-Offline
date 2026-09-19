package com.fahendrena.teacherassistant.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.fahendrena.teacherassistant.data.Preparation
import com.fahendrena.teacherassistant.data.SchoolClass
import com.fahendrena.teacherassistant.data.TeacherProfile
import java.io.File
import java.io.FileOutputStream

/**
 * Genere un PDF A4 professionnel pour une fiche de preparation, entierement
 * hors ligne (android.graphics.pdf.PdfDocument, aucune bibliotheque cloud).
 * Ordre des pages conforme au cahier des charges : fiche, contenu,
 * exercices, corrige, evaluation, devoir.
 */
object PreparationPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 16f

    private enum class TextStyle { TITLE, HEADER, BODY }
    private data class Block(val text: String, val style: TextStyle)

    fun export(
        context: Context,
        preparation: Preparation,
        profile: TeacherProfile?,
        schoolClass: SchoolClass?
    ): Uri {
        val document = PdfDocument()

        val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 13f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 11f }

        val blocks = buildBlocks(preparation, profile, schoolClass)
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas: Canvas = page.canvas
        var y = MARGIN

        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            y = MARGIN
        }

        for (block in blocks) {
            if (block.text.isBlank()) continue
            val paint = when (block.style) {
                TextStyle.TITLE -> titlePaint
                TextStyle.HEADER -> headerPaint
                TextStyle.BODY -> bodyPaint
            }
            val wrapped = wrapText(block.text, paint, PAGE_WIDTH - 2 * MARGIN)
            for (line in wrapped) {
                if (y > PAGE_HEIGHT - MARGIN) newPage()
                canvas.drawText(line, MARGIN, y, paint)
                y += LINE_HEIGHT
            }
            if (block.style != TextStyle.BODY) y += LINE_HEIGHT / 2 else y += LINE_HEIGHT / 4
        }
        document.finishPage(page)

        val exportsDir = File(context.filesDir, "exports").apply { mkdirs() }
        val safeName = preparation.titreLecon.replace(Regex("[^A-Za-z0-9]+"), "_")
        val outFile = File(exportsDir, "Fiche_${safeName}_${preparation.id}.pdf")
        FileOutputStream(outFile).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    }

    private fun buildBlocks(p: Preparation, profile: TeacherProfile?, c: SchoolClass?): List<Block> = buildList {
        add(Block("FICHE DE PREPARATION", TextStyle.TITLE))
        add(Block("Etablissement : ${profile?.etablissement.orEmpty()}", TextStyle.BODY))
        add(Block("Enseignant : ${profile?.nom.orEmpty()}", TextStyle.BODY))
        add(Block("Matiere : ${p.matiere}   Niveau : ${p.niveau}   Classe : ${c?.nom.orEmpty()}", TextStyle.BODY))
        add(Block("Date : ${p.date}   Duree : ${p.dureeMinutes} min   Seance n. ${p.numeroSeance}", TextStyle.BODY))
        add(Block("Theme : ${p.theme}", TextStyle.BODY))
        add(Block("Titre de la lecon : ${p.titreLecon}", TextStyle.BODY))

        add(Block("OBJECTIF GENERAL", TextStyle.HEADER))
        add(Block(p.objectifGeneral, TextStyle.BODY))

        add(Block("OBJECTIFS SPECIFIQUES", TextStyle.HEADER))
        add(Block(p.objectifsSpecifiques, TextStyle.BODY))

        add(Block("COMPETENCES", TextStyle.HEADER))
        add(Block(p.competences, TextStyle.BODY))

        add(Block("PREREQUIS", TextStyle.HEADER))
        add(Block(p.prerequis, TextStyle.BODY))

        add(Block("MATERIEL", TextStyle.HEADER))
        add(Block(p.materiel, TextStyle.BODY))

        add(Block("METHODE PEDAGOGIQUE", TextStyle.HEADER))
        add(Block(p.methodePedagogique, TextStyle.BODY))

        add(Block("DEROULEMENT", TextStyle.HEADER))
        add(Block(p.deroulementJson, TextStyle.BODY))

        add(Block("CONTENU DE LA LECON", TextStyle.TITLE))
        add(Block(p.contenuLecon, TextStyle.BODY))

        add(Block("VOCABULAIRE", TextStyle.HEADER))
        add(Block(p.vocabulaireJson, TextStyle.BODY))

        add(Block("GRAMMAIRE", TextStyle.HEADER))
        add(Block(p.grammaire, TextStyle.BODY))

        add(Block("ACTIVITES", TextStyle.TITLE))
        add(Block(p.activites, TextStyle.BODY))

        add(Block("EXERCICES", TextStyle.TITLE))
        add(Block(p.exercices, TextStyle.BODY))

        add(Block("CORRIGE", TextStyle.TITLE))
        add(Block(p.corrige, TextStyle.BODY))

        add(Block("EVALUATION", TextStyle.TITLE))
        add(Block(p.evaluation, TextStyle.BODY))

        add(Block("DEVOIR", TextStyle.TITLE))
        add(Block(p.devoir, TextStyle.BODY))
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        for (paragraph in text.split("\n")) {
            if (paragraph.isBlank()) {
                result.add("")
                continue
            }
            var line = StringBuilder()
            for (word in paragraph.split(" ")) {
                val test = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(test) > maxWidth) {
                    if (line.isNotEmpty()) result.add(line.toString())
                    line = StringBuilder(word)
                } else {
                    line = StringBuilder(test)
                }
            }
            if (line.isNotEmpty()) result.add(line.toString())
        }
        return result
    }
}
