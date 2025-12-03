package com.jdc.download;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class StooqDownloader {

  private final HttpClient client = HttpClient.newHttpClient();

  // see https://chatgpt.com/g/g-p-68e394f7d2588191be07ef22e8e6952b/c/691cde60-a480-8326-8cee-463b0856bc0d
  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("Working dir = " + System.getProperty("user.dir"));
    StooqDownloader dl = new StooqDownloader();
//    dl.downloadDailyCsv("spy.us", "20000101", null, Path.of("data/SPY_stooq.csv"));
//    dl.downloadDailyCsv("spy.us", Path.of("data/SPY_stooq.csv"));
//    dl.downloadDailyCsv("efa.us", Path.of("data/EFA_stooq.csv"));
//    dl.downloadDailyCsv("qqq.us", Path.of("data/QQQ_stooq.csv"));
//    dl.downloadDailyCsv("iwm.us", Path.of("data/IWM_stooq.csv"));
//    dl.downloadDailyCsv("ief.us", Path.of("data/IEF_stooq.csv"));

//    dl.downloadDailyCsv("spy.us", Path.of("data/SPY_stooq.csv"));
//    dl.downloadDailyCsv("efa.us", Path.of("data/EFA_stooq.csv"));
//    dl.downloadDailyCsv("qqq.us", Path.of("data/QQQ_stooq.csv"));
//    dl.downloadDailyCsv("iwm.us", Path.of("data/IWM_stooq.csv"));
//    dl.downloadDailyCsv("ief.us", Path.of("data/IEF_stooq.csv"));
//    dl.downloadDailyCsv("nvda.us", Path.of("data/NVDA_stooq.csv"));
//    dl.downloadDailyCsv("gld.us", Path.of("data/GLD_stooq.csv"));
//    dl.downloadDailyCsv("slv.us", Path.of("data/SLV_stooq.csv"));
//    dl.downloadDailyCsv("tqqq.us", Path.of("data/TQQQ_stooq.csv"));
//    dl.downloadDailyCsv("soxl.us", Path.of("data/SOXL_stooq.csv"));
//    dl.downloadDailyCsv("sqqq.us", Path.of("data/SQQQ_stooq.csv"));
//    dl.downloadDailyCsv("goog.us", Path.of("data/GOOG_stooq.csv"));
//    dl.downloadDailyCsv("uber.us", Path.of("data/UBER_stooq.csv"));
//    dl.downloadDailyCsv("coin.us", Path.of("data/COIN_stooq.csv"));
//    dl.downloadDailyCsv("bmnr.us", Path.of("data/BMNR_stooq.csv"));
//    dl.downloadDailyCsv("crcl.us", Path.of("data/CRCL_stooq.csv"));
//    dl.downloadDailyCsv("cde.us", Path.of("data/CDE_stooq.csv"));
//    dl.downloadDailyCsv("cper.us", Path.of("data/CPER_stooq.csv"));
//    dl.downloadDailyCsv("uup.us", Path.of("data/UUP_stooq.csv"));
//    dl.downloadDailyCsv("tlt.us", Path.of("data/TLT_stooq.csv"));
//    dl.downloadDailyCsv("baba.us", Path.of("data/BABA_stooq.csv"));
//    dl.downloadDailyCsv("amc.us", Path.of("data/AMC_stooq.csv"));
//    dl.downloadDailyCsv("uamy.us", Path.of("data/UAMY_stooq.csv"));
//    dl.downloadDailyCsv("app.us", Path.of("data/APP_stooq.csv"));
//    dl.downloadDailyCsv("open.us", Path.of("data/OPEN_stooq.csv"));
//    dl.downloadDailyCsv("opfi.us", Path.of("data/OPFI_stooq.csv"));
//    dl.downloadDailyCsv("ura.us", Path.of("data/URA_stooq.csv"));
//    dl.downloadDailyCsv("nu.us", Path.of("data/NU_stooq.csv"));

//    dl.downloadDailyCsv("smh.us", Path.of("data/SMH_stooq.csv"));
//    dl.downloadDailyCsv("emqq.us", Path.of("data/EMQQ_stooq.csv"));
//    dl.downloadDailyCsv("oxy.us", Path.of("data/OXY_stooq.csv"));
    dl.downloadDailyCsv("remx.us", Path.of("data/REMX_stooq.csv"));
    dl.downloadDailyCsv("rgld.us", Path.of("data/RGLD_stooq.csv"));
    dl.downloadDailyCsv("pltr.us", Path.of("data/PLTR_stooq.csv"));
    dl.downloadDailyCsv("aapl.us", Path.of("data/AAPL_stooq.csv"));
    dl.downloadDailyCsv("msft.us", Path.of("data/MSFT_stooq.csv"));
    dl.downloadDailyCsv("tsla.us", Path.of("data/TSLA_stooq.csv"));
    dl.downloadDailyCsv("gdx.us", Path.of("data/GDX_stooq.csv"));


    // commodities ( https://chatgpt.com/g/g-p-68c941c2de0c8191853cc664de5f0ab0-allerlei/c/692c3166-89e8-8329-9c8f-e66ef1079255 )
//    dl.downloadDailyCsv("dba.us", Path.of("data/DBA_stooq.csv"));
//    dl.downloadDailyCsv("corn.us", Path.of("data/CORN_stooq.csv"));
//    dl.downloadDailyCsv("soyb.us", Path.of("data/SOYB_stooq.csv"));
//    dl.downloadDailyCsv("weat.us", Path.of("data/WEAT_stooq.csv"));
//    dl.downloadDailyCsv("dbb.us", Path.of("data/DBB_stooq.csv"));
//    dl.downloadDailyCsv("bcim.us", Path.of("data/BCIM_stooq.csv"));
//    dl.downloadDailyCsv("uso.us", Path.of("data/USO_stooq.csv"));
//    dl.downloadDailyCsv("bno.us", Path.of("data/BNO_stooq.csv"));
//    dl.downloadDailyCsv("fgdl.us", Path.of("data/FGDL_stooq.csv"));
//    dl.downloadDailyCsv("plg.us", Path.of("data/PLG_stooq.csv"));
//    dl.downloadDailyCsv("phpt.XX", Path.of("data/PHPT_stooq.csv"));
//    dl.downloadDailyCsv("phpd.XX", Path.of("data/PHPD_stooq.csv"));
//    dl.downloadDailyCsv("phpd.XX", Path.of("data/PHPD_stooq.csv"));
//    dl.downloadDailyCsv("nick.XX", Path.of("data/NICK_stooq.csv"));
//    dl.downloadDailyCsv("nick.XX", Path.of("data/NICK_stooq.csv"));
//    dl.downloadDailyCsv("crud.XX", Path.of("data/CRUD_stooq.csv"));
//    dl.downloadDailyCsv("ngas.XX", Path.of("data/NGAS_stooq.csv"));
//    dl.downloadDailyCsv("coff.XX", Path.of("data/COFF_stooq.csv"));
//    dl.downloadDailyCsv("suga.XX", Path.of("data/SUGA_stooq.csv"));
//    dl.downloadDailyCsv("coco.XX", Path.of("data/COCO_stooq.csv"));
//    dl.downloadDailyCsv("coco.XX", Path.of("data/COCO_stooq.csv"));
//    dl.downloadDailyCsv("cotn.XX", Path.of("data/COTN_stooq.csv"));
//    dl.downloadDailyCsv("cow.XX", Path.of("data/COW_stooq.csv"));



  }

  public void downloadDailyCsv(String stooqSymbol, Path targetFile)
      throws IOException, InterruptedException {

    String url = "https://stooq.com/q/d/l/?s=" + stooqSymbol + "&i=d";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        .GET()
        .build();

    HttpResponse<String> response =
        client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("HTTP status = " + response.statusCode());
    String body = response.body();
    System.out.println("First 80 chars: " +
        body.substring(0, Math.min(80, body.length())));

    if (!body.startsWith("Date,")) {
      throw new IOException("Stooq did not return CSV:\n" + body);
    }

    Files.createDirectories(targetFile.getParent());
    Files.writeString(targetFile, body);
  }
}
