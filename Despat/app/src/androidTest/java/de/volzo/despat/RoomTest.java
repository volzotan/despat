package de.volzo.despat;

import androidx.room.Room;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import de.volzo.despat.persistence.AppDatabase;
import de.volzo.despat.persistence.Status;
import de.volzo.despat.persistence.StatusDao;

/**
 * Created by volzotan on 17.01.18.
 */

@RunWith(AndroidJUnit4.class)
public class RoomTest {
    private StatusDao statusDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        statusDao = db.statusDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void writeUserAndReadInList() throws Exception {
//        User user = TestUtil.createUser(3);
//        user.setName("george");
//        mUserDao.insert(user);
//        List<User> byName = mUserDao.findUsersByName("george");
//        assertThat(byName.get(0), equalTo(user));
    }
}

