package utils

import java.io.{File, InputStreamReader}

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import scala.collection.JavaConverters._

object GoogleSheets {
  val applicationName = "train-stamp-rally"
  val dataStoreDir = new File("conf/")
  val dataStoreFactory = new FileDataStoreFactory(dataStoreDir)
  val jsonFactory = JacksonFactory.getDefaultInstance
  val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  val scopes = List(SheetsScopes.SPREADSHEETS_READONLY).asJava

  def authorize(): Credential = {
    val in = getClass.getResourceAsStream("/client_secret.json")
    val clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in))
    println(httpTransport)
    println(jsonFactory)
    println(clientSecrets)
    println(scopes)
    val flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, scopes)
        .setDataStoreFactory(dataStoreFactory)
        .setAccessType("offline")
        .build()
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(null)
  }

  def getSheetServices: Sheets = {
    val credential = authorize()
    new Sheets.Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(applicationName)
        .build()
  }
}
