package hu.deik.boozepal.dao.test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hu.deik.boozepal.common.contants.BoozePalConstants;
import hu.deik.boozepal.common.entity.Coordinate;
import hu.deik.boozepal.common.entity.PalRequest;
import hu.deik.boozepal.common.entity.Pub;
import hu.deik.boozepal.common.entity.Role;
import hu.deik.boozepal.common.entity.User;
import hu.deik.boozepal.core.repo.PubRepository;
import hu.deik.boozepal.core.repo.RoleRepository;
import hu.deik.boozepal.core.repo.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-core-test.xml" })
@Rollback
public class UserRepositoryTest {

    private static final String TEST_EMAIL_COM = "Test@email.com";
    private static final String TEST_PASSWORD = "TestPassword";
    private static final String TEST_USER = "TestUser";

    private User user;

    @Autowired
    private UserRepository userDao;

    @Autowired
    private RoleRepository roleDao;
    
    @Autowired
    private PubRepository pubDao;

    @Test
    public void testUserDaoNotNull() {
        Assert.assertNotNull(userDao);
    }

    @Test
    public void testSaveNewUser() {
        user = createUser();
        User savedUser = userDao.save(user);
        Assert.assertNotNull(savedUser.getId());
    }

    @Test
    public void testFindSavedUserByUserName() {
        user = createUser();
        userDao.save(user);
        User foundUser = userDao.findByUsername(TEST_USER);
        Assert.assertEquals(TEST_USER, foundUser.getUsername());
    }

    @Test
    public void testFindSavedUserByEmail() {
        user = createUser();
        userDao.save(user);
        User foundUser = userDao.findByEmail(TEST_EMAIL_COM);
        Assert.assertEquals(TEST_EMAIL_COM, foundUser.getEmail());
    }

    @Test
    public void testUpdateUserCoordinate() {
        user = createUser();
        User savedUser = userDao.save(user);
        double latitude = 11.0;
        double altitude = 12.0;
        userDao.updateUserCoordinate(latitude, altitude, savedUser.getId());
        User findOne = userDao.findOne(savedUser.getId());
        Assert.assertEquals(latitude, findOne.getLastKnownCoordinate().getLatitude(), 0.0);
        Assert.assertEquals(altitude, findOne.getLastKnownCoordinate().getLongitude(), 0.0);
        // userDao.delete(savedUser.getId());
    }

    @Test
    public void testUpdatePriceCategory() {
        user = createUser();
        User savedUser = userDao.save(user);
        int priceCategory = 4;
        userDao.updatePriceCategory(priceCategory, savedUser.getId());
        User findOne = userDao.findOne(savedUser.getId());
        Assert.assertEquals(priceCategory, findOne.getPriceCategory(), 0.0);
        // userDao.delete(savedUser.getId());
    }

    @Test
    public void testActivateUser() {
        user = createUser();
        User savedUser = userDao.save(user);
        userDao.activateUser(savedUser.getId());
        User findOne = userDao.findOne(savedUser.getId());
        Assert.assertFalse(findOne.isRemove());
    }

    @Test
    public void testDeActivateUser() {
        user = createUser();
        User savedUser = userDao.save(user);
        userDao.deactivateUser(savedUser.getId());
        User findOne = userDao.findOne(savedUser.getId());
        Assert.assertTrue(findOne.isRemove());
    }

    @Test
    public void testFindOnlineUsers() {
        User onlineUser = createUser("onlineUser", "online@online.com");
        onlineUser.setLoggedIn(true);
        User offlineUser = createUser("offlineUser", "offline@offline.com");
        offlineUser.setLoggedIn(false);
        userDao.save(onlineUser);
        userDao.save(offlineUser);

        List<User> findByLoggedIn = userDao.findOnlineUsers();
        Assert.assertTrue(findByLoggedIn.contains(onlineUser));
        Assert.assertFalse(findByLoggedIn.contains(offlineUser));
        userDao.delete(onlineUser);
        userDao.delete(offlineUser);

    }
    @Test
    public void testFindById() {
        User user = createUser("onlineUser","online@online.com");
        user = userDao.save(user);
        User dbUser = userDao.findById(user.getId());
        Assert.assertNotNull(dbUser);
        Assert.assertTrue(user.equals(dbUser));
        userDao.delete(user);
    }
    @Test
    public void testFindByUndefinedId() {
        User user = createUser("onlineUser","online@online.com");
        user = userDao.save(user);
        User dbUser = userDao.findById(9999l);
        Assert.assertNull(dbUser);
        userDao.delete(user);
    }

    @Test
    public void testFindByRoleUser() {
//        User nonUserUser = createUser("nonUserUser", "nonUserUser@email.com");
//        userDao.save(nonUserUser);
//        User createUserWithUserRole = createUserWithUserRole("userWithUserRole1", "userWithUserRole1@email.com");
//        userDao.save(createUserWithUserRole);
//        User createUserWithUserRole2 = createUserWithUserRole("userWithUserRole2", "userWithUserRole2@email.com");
//        userDao.save(createUserWithUserRole2);

        // TODO Nandi - átírni matcherre.
        List<User> findByRoleUser = userDao.findByRoleUser();
        Assert.assertFalse(findByRoleUser.isEmpty());

    }
    
    @Test
    public void testCountGivenPriceCategory() {
        Integer lowest = userDao.countGivenPriceCategory(1);
        Assert.assertEquals(Integer.valueOf(1), lowest);
        Integer medium = userDao.countGivenPriceCategory(3);
        Assert.assertEquals(Integer.valueOf(1), medium);
        Integer highest = userDao.countGivenPriceCategory(5);
        Assert.assertEquals(Integer.valueOf(0), highest);
    }

    @Test
    public void testMakePals() {
        Pub pub = Pub.builder().name("Ibolya").openHours("24:00").priceCategory(5).build();
        pubDao.save(pub);
        Pub ibolya = pubDao.findByName("Ibolya");
        User viktor = User.builder().username("Viktor").email("viki@viki.com").password("xxx").build();
        User savedViktor = userDao.save(viktor);
        User nandi = User.builder().username("Nandi").email("nandi@viki.com").password("xxx").build();
        User savedNandi = userDao.save(nandi);
        
        savedViktor.getActualPals().put(savedNandi.getId(), PalRequest.builder().date(new Date()).pub(ibolya).build());
        User save = userDao.save(savedViktor);
        save.getActualPals().put(savedNandi.getId(), PalRequest.builder().date(new Date()).pub(ibolya).build());
        save = userDao.save(save);
        
        PalRequest palRequest = save.getActualPals().get(savedNandi.getId());
        Assert.assertEquals(ibolya, palRequest.getPub());
    }
    
    @After
    public void tearDown() {
        if (user != null && user.getId() != null)
            userDao.delete(user);
    }

    //@formatter:off
    private User createUser() {
        return User.builder()
                .username(TEST_USER)
                .email(TEST_EMAIL_COM)
                .password(TEST_PASSWORD)
                .remove(false)
                .lastKnownCoordinate(new Coordinate(10.0, 10.0))
                .priceCategory(3)
                .build();
    }
    
    private User createUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password(TEST_PASSWORD)
                .remove(false)
                .lastKnownCoordinate(new Coordinate(10.0, 10.0))
                .priceCategory(3)
                .build();
    }
    
    private User createUserWithUserRole(String username, String email) {
        Role userRole = roleDao.findByRoleName(BoozePalConstants.ROLE_USER);
        return User.builder()
                .username(username)
                .email(email)
                .password(TEST_PASSWORD)
                .remove(false)
                .roles(Arrays.asList(userRole == null ? createAndGetUserRole():userRole))
                .lastKnownCoordinate(new Coordinate(10.0, 10.0))
                .priceCategory(3)
                .build();
    }

    private Role createAndGetUserRole() {
        return roleDao.save(new Role(BoozePalConstants.ROLE_USER));
    }
   //@formatter:on

}
