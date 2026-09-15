from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor


OUT = Path(r"C:\Users\hp\Desktop\rag-project\docs\Introduction_Assistance_Orange.docx")
ORANGE = "F16E00"


def set_cell_border(paragraph):
    p_pr = paragraph._p.get_or_add_pPr()
    borders = OxmlElement("w:pBdr")
    bottom = OxmlElement("w:bottom")
    bottom.set(qn("w:val"), "single")
    bottom.set(qn("w:sz"), "16")
    bottom.set(qn("w:space"), "5")
    bottom.set(qn("w:color"), ORANGE)
    borders.append(bottom)
    p_pr.append(borders)


def add_body(doc, text):
    paragraph = doc.add_paragraph()
    paragraph.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    paragraph.paragraph_format.first_line_indent = Cm(0.7)
    paragraph.paragraph_format.line_spacing = 1.12
    paragraph.paragraph_format.space_after = Pt(7)
    paragraph.paragraph_format.widow_control = True
    run = paragraph.add_run(text)
    run.font.name = "Arial"
    run.font.size = Pt(10.5)
    run.font.color.rgb = RGBColor(25, 25, 25)


def build():
    doc = Document()
    section = doc.sections[0]
    section.page_width = Cm(21)
    section.page_height = Cm(29.7)
    section.top_margin = Cm(2.0)
    section.bottom_margin = Cm(2.0)
    section.left_margin = Cm(2.3)
    section.right_margin = Cm(2.3)

    normal = doc.styles["Normal"]
    normal.font.name = "Arial"
    normal.font.size = Pt(10.5)

    title = doc.add_paragraph()
    title.alignment = WD_ALIGN_PARAGRAPH.LEFT
    title.paragraph_format.space_after = Pt(16)
    run = title.add_run("Introduction")
    run.bold = True
    run.font.name = "Arial"
    run.font.size = Pt(22)
    run.font.color.rgb = RGBColor(17, 17, 17)
    set_cell_border(title)

    paragraphs = [
        "La transformation numérique modifie profondément la manière dont les entreprises accompagnent leurs clients. Dans le secteur des télécommunications, les utilisateurs recherchent des réponses rapides, compréhensibles et disponibles à tout moment lorsqu’ils rencontrent un problème avec leur connexion, leur Livebox ou leurs équipements. Pourtant, les informations utiles sont souvent réparties dans de nombreuses pages d’assistance, ce qui peut rendre la recherche longue et difficile. C’est dans ce contexte que s’inscrit le projet « Assistance Orange », réalisé afin de proposer un accès plus simple et plus direct à la documentation technique disponible.",
        "Le projet consiste à développer une application web sécurisée intégrant un assistant conversationnel fondé sur une approche de génération augmentée par récupération, appelée RAG. Son principe est de rechercher les passages les plus pertinents dans une base documentaire avant de demander à un modèle d’intelligence artificielle de formuler la réponse. Cette méthode permet de produire des réponses liées aux sources réellement ajoutées dans l’application et de réduire le risque de générer des informations sans rapport avec la documentation disponible.",
        "L’application distingue deux profils. L’administrateur peut ajouter des adresses de pages provenant du site Orange Assistance afin d’enrichir la base de connaissances. Le contenu de chaque page est récupéré, nettoyé, découpé en fragments puis transformé en représentations numériques appelées embeddings. Ces vecteurs sont enregistrés dans PostgreSQL grâce à l’extension pgvector. L’utilisateur, quant à lui, peut créer une conversation, poser une question et consulter l’historique des messages échangés avec l’assistant. La suppression d’une conversation est également prévue afin de faciliter la gestion de l’espace personnel.",
        "Sur le plan technique, l’interface a été développée avec Angular, tandis que le backend repose sur Spring Boot et Spring AI. Keycloak assure l’authentification, l’inscription et la gestion des rôles ADMIN et USER. LM Studio fournit localement le modèle d’embedding utilisé pour convertir les textes et les questions en vecteurs comparables. PostgreSQL conserve les conversations, les messages et les fragments documentaires, alors que le modèle de génération accessible par l’API Groq construit la réponse finale à partir du contexte retrouvé. La réponse est transmise progressivement au navigateur afin d’améliorer le confort d’utilisation.",
        "Une attention particulière a également été portée à l’expérience utilisateur. L’interface reprend une identité visuelle inspirée d’Orange, propose une navigation claire et adapte les libellés au profil connecté. Elle permet de choisir la langue de l’interface et l’assistant doit répondre dans la langue utilisée dans la question. L’accès à l’ajout des sources est réservé à l’administrateur, tandis que le client dispose principalement du service de conversation et de son historique.",
        "L’objectif de ce travail est donc de concevoir une solution cohérente reliant l’authentification, l’indexation documentaire, la recherche vectorielle et la génération de réponses. Au-delà de la réalisation technique, le projet met en évidence l’intérêt d’une architecture RAG pour valoriser une documentation existante et offrir un service d’assistance plus accessible. Le présent travail décrit les besoins, l’architecture, les choix de conception et les principales fonctionnalités mises en œuvre, ainsi que les conditions nécessaires au bon fonctionnement et à l’évolution future de l’application.",
    ]

    for text in paragraphs:
        add_body(doc, text)

    footer = section.footer.paragraphs[0]
    footer.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    footer_run = footer.add_run("Assistance Orange")
    footer_run.font.name = "Arial"
    footer_run.font.size = Pt(8)
    footer_run.font.color.rgb = RGBColor(110, 110, 110)

    doc.core_properties.title = "Introduction — Assistance Orange"
    doc.core_properties.subject = "Introduction du projet de stage"
    doc.core_properties.author = "Projet Assistance Orange"
    doc.save(OUT)
    print(OUT)


if __name__ == "__main__":
    build()
