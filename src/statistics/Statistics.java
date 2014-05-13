/*
 * (C) Copyright 2014 Mikaël Castellani
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package statistics;

import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import common.DBHelper;
import common.Event;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mikaelcastellani
 */
public class Statistics {

    public static void main(String[] args) {

        DBHelper db = DBHelper.getInstance();

        DBCursor cursor = db.findMatrixRows().addOption(Bytes.QUERYOPTION_NOTIMEOUT);

        int counts[] = new int[12 * 2];
        float totalEntries = 0.0f;
        while (cursor.hasNext()) {
            DBObject item = cursor.next();
            int count = 0;
            Boolean hasAtLeastOneEvent = false;
            for (Event.TYPE everyType : Event.TYPE.values()) {

                if ((Boolean) item.get("has_twitter_" + everyType.name().toLowerCase()) == true) {
                    hasAtLeastOneEvent = true;
                    ++counts[count];
                }

                if ((Boolean) item.get("has_facebook_" + everyType.name().toLowerCase()) == true) {
                    hasAtLeastOneEvent = true;
                    ++counts[count + 1];
                }
                count += 2;
            }
            if (hasAtLeastOneEvent) {
                ++totalEntries;
            }
        }
        int count = 0;
        for (Event.TYPE everyType : Event.TYPE.values()) {

            System.out.println(everyType.name() + " : T " + counts[count] + " / " + counts[count] / totalEntries + " per album / F " + counts[count + 1] + " / " + counts[count + 1] / totalEntries + " per album");
            count += 2;
        }
        System.out.println("Total number of albums with events : "+totalEntries);

    }

}
