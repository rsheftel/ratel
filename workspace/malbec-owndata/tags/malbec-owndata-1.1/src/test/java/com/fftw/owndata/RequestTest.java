package com.fftw.owndata;

import org.testng.annotations.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.fftw.metadb.domain.Tick;

/**
 * Request Tester.
 *
 * @author mfranz
 * @version $Revision$, $Date$
 * @created May 20, 2008
 * @since 1.0
 */
public class RequestTest {


    @Test(groups =
            {
                    "unittest"
                    })
    public void testGetLiveTicks() {
        Request request = new Request(1, "TestRequest");
        List<Tick> tickLiveList = request.getTickLiveList();

        Tick tick = new Tick(new Date(), 12.34d, 34.56d);
        tickLiveList.add(tick);

        double[][] liveTicks = request.getLiveTicks();

        assert liveTicks.length == 1 : "wrong number of live ticks";
    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testGetHistoryTicks() throws Exception {
        Request request = new Request(1, "TestRequest");
        List<Tick> tickHistoryList = request.getTickHistory();

        Date date = new Date();
        tickHistoryList.add(new Tick(date, 12.34d, 100D));
        tickHistoryList.add(new Tick(date, 23.45d, 100D));
        tickHistoryList.add(new Tick(date, 34.56d, 100D));

        double[][] historyTicks = request.getHistoryTicks();

        assert historyTicks.length == 3 : "Wrong number of ticks";


    }

    @Test(groups =
            {
                    "unittest"
                    })
    public void testOnData() {
        Request request = new Request(1, "TestRequest");

        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("OpenPrice", "22.5");
        dataMap.put("HighPrice", "25.2");
        dataMap.put("LowPrice", "22.2");

        dataMap.put("Timestamp", "2008/06/20 08:00:00");
        dataMap.put("LastPrice", "23.0");
        dataMap.put("LastVolume", "12340");

        try {
            request.onData(dataMap);
        } catch (Exception e) {
            assert false : "Failed to parse dataMap record";
        }
    }

}
