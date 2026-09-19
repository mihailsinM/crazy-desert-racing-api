package com.crazydesert.racing.config;

import com.crazydesert.racing.DesertLiveItem;
import com.crazydesert.racing.Race;
import com.crazydesert.racing.RaceCar;
import com.crazydesert.racing.RaceRegistration;
import com.crazydesert.racing.User;
import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;
import com.crazydesert.racing.enums.MembershipTier;
import com.crazydesert.racing.enums.RaceStatus;
import com.crazydesert.racing.enums.Role;
import com.crazydesert.racing.repository.DesertLiveItemRepository;
import com.crazydesert.racing.repository.RaceCarRepository;
import com.crazydesert.racing.repository.RaceRegistrationRepository;
import com.crazydesert.racing.repository.RaceRepository;
import com.crazydesert.racing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(20)
@ConditionalOnProperty(name = "crazy.demo-seed.enabled", havingValue = "true")
public class DemoWorldSeeder implements ApplicationRunner {

    static final String REQUIRED_CONFIRMATION = "RESET_LOCAL_DEMO_WORLD";

    private final UserRepository userRepository;
    private final RaceCarRepository raceCarRepository;
    private final RaceRepository raceRepository;
    private final RaceRegistrationRepository raceRegistrationRepository;
    private final DesertLiveItemRepository desertLiveItemRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String ownerEmail;
    private final String demoPassword;
    private final String resetConfirmation;

    public DemoWorldSeeder(
            UserRepository userRepository,
            RaceCarRepository raceCarRepository,
            RaceRepository raceRepository,
            RaceRegistrationRepository raceRegistrationRepository,
            DesertLiveItemRepository desertLiveItemRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${crazy.super-admin.email}") String ownerEmail,
            @Value("${crazy.demo-seed.user-password}") String demoPassword,
            @Value("${crazy.demo-seed.reset-confirmation:}") String resetConfirmation) {

        this.userRepository = userRepository;
        this.raceCarRepository = raceCarRepository;
        this.raceRepository = raceRepository;
        this.raceRegistrationRepository = raceRegistrationRepository;
        this.desertLiveItemRepository = desertLiveItemRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.ownerEmail = requireText(ownerEmail, "Owner email is required for demo seed");
        this.demoPassword = requireText(demoPassword, "Demo user password is required");
        this.resetConfirmation = resetConfirmation;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!REQUIRED_CONFIRMATION.equals(resetConfirmation)) {
            throw new IllegalStateException(
                    "Demo reset blocked: set CRAZY_DEMO_SEED_RESET_CONFIRMATION="
                            + REQUIRED_CONFIRMATION
            );
        }

        User owner = userRepository.findByEmail(ownerEmail.trim().toLowerCase())
                .filter(user -> user.getRole() == Role.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException(
                        "Protected SUPER_ADMIN must exist before demo seed"
                ));

        clearWorldExceptOwner(owner.getId());

        List<User> users = userRepository.saveAll(createUsers());
        List<RaceCar> cars = raceCarRepository.saveAll(createCars(users));
        List<Race> races = raceRepository.saveAll(createRaces());

        raceRegistrationRepository.saveAll(createRegistrations(users, cars, races));
        desertLiveItemRepository.saveAll(createPublications(owner, users, races));
    }

    private void clearWorldExceptOwner(Long ownerId) {
        jdbcTemplate.update("delete from user_photo_reports");
        jdbcTemplate.update("delete from race_registrations");
        jdbcTemplate.update("delete from desert_live_images");
        jdbcTemplate.update("delete from desert_live_items");
        jdbcTemplate.update(
                "update users set profile_photo_id = null, profile_card_photo_id = null where id <> ?",
                ownerId
        );
        jdbcTemplate.update("""
                delete from media_images image
                where not exists (
                    select 1 from user_photos photo
                    where photo.owner_id = ? and photo.image_key = image.image_key
                )
                and not exists (
                    select 1 from race_cars car
                    where car.user_id = ? and car.image_key = image.image_key
                )
                """, ownerId, ownerId);
        jdbcTemplate.update("delete from user_photos where owner_id <> ?", ownerId);
        jdbcTemplate.update("delete from race_cars where user_id <> ?", ownerId);
        jdbcTemplate.update("delete from users where id <> ?", ownerId);
        jdbcTemplate.update("delete from races");
    }

    private List<User> createUsers() {
        String encodedPassword = passwordEncoder.encode(demoPassword);
        LocalDateTime membershipExpiry = LocalDateTime.now().plusYears(1);
        List<UserSeed> seeds = List.of(
                new UserSeed("Noa Ben-David", 29, "Be'er Sheva", "Desert rally driver and navigation fan.", "B", MembershipTier.GOLD),
                new UserSeed("Daniel Cohen", 34, "Tel Aviv", "Hybrid performance builder and weekend racer.", "B", MembershipTier.SILVER),
                new UserSeed("Maya Levi", 27, "Eilat", "Festival photographer chasing desert light.", "B", MembershipTier.PLATINUM),
                new UserSeed("Ariel Mizrahi", 41, "Dimona", "Off-road mechanic and recovery volunteer.", "C", MembershipTier.GOLD),
                new UserSeed("Yael Shahar", 32, "Haifa", "Road-trip organizer and community storyteller.", "B", MembershipTier.STANDARD),
                new UserSeed("Omer Azulay", 25, "Ashdod", "First season racer learning every kilometer.", "B", MembershipTier.STANDARD),
                new UserSeed("Lior Avraham", 37, "Jerusalem", "Camp chef, music lover and desert explorer.", "B", MembershipTier.SILVER),
                new UserSeed("Tamar Katz", 30, "Netanya", "EV engineer interested in endurance racing.", "B", MembershipTier.GOLD),
                new UserSeed("Eitan Peretz", 44, "Arad", "Veteran navigator who knows the Negev trails.", "C", MembershipTier.PLATINUM),
                new UserSeed("Shira Malka", 26, "Rishon LeZion", "Designer, festival volunteer and new driver.", "B", MembershipTier.STANDARD),
                new UserSeed("Yonatan Bar", 39, "Hadera", "Family racer and careful car collector.", "B", MembershipTier.SILVER),
                new UserSeed("Neta Golan", 35, "Sde Boker", "Desert guide focused on responsible travel.", "B", MembershipTier.GOLD),
                new UserSeed("Roi Dahan", 31, "Holon", "Track-day regular and hybrid tuning enthusiast.", "B", MembershipTier.SILVER),
                new UserSeed("Adi Romano", 28, "Kfar Saba", "Community host connecting drivers and festivals.", "B", MembershipTier.STANDARD)
        );

        List<User> users = new ArrayList<>();
        for (int index = 0; index < seeds.size(); index++) {
            UserSeed seed = seeds.get(index);
            User user = new User();
            user.setName(seed.name());
            user.setAge(seed.age());
            user.setEmail("demo.driver%02d@crazydesert.local".formatted(index + 1));
            user.setPassword(encodedPassword);
            user.setRole(Role.USER);
            user.setLicenseCategory(seed.licenseCategory());
            user.setLicenseVerified(index < 11);
            user.setProfileLocation(seed.location());
            user.setProfileBio(seed.bio());
            user.setShowCars(true);
            user.setShowRaceHistory(true);
            user.setShowPhotos(true);
            user.setMembershipTier(seed.membershipTier());
            if (seed.membershipTier() != MembershipTier.STANDARD) {
                user.setMembershipExpiresAt(membershipExpiry);
            }
            users.add(user);
        }
        return users;
    }

    private List<RaceCar> createCars(List<User> users) {
        List<CarSeed> seeds = List.of(
                new CarSeed("Prius Desert Pulse", "Toyota", 196),
                new CarSeed("Corolla Sand Runner", "Toyota", 138),
                new CarSeed("Elantra Dune Line", "Hyundai", 139),
                new CarSeed("Octavia iV Horizon", "Skoda", 204),
                new CarSeed("S60 Recharge", "Volvo", 455),
                new CarSeed("Seal U DM-i", "BYD", 218),
                new CarSeed("Niro Trail Hybrid", "Kia", 139),
                new CarSeed("Civic e:HEV", "Honda", 184),
                new CarSeed("Lexus NX Desert", "Lexus", 240),
                new CarSeed("Kona Hybrid Sun", "Hyundai", 141),
                new CarSeed("Yaris Cross Rally", "Toyota", 116),
                new CarSeed("Qashqai e-Power", "Nissan", 190),
                new CarSeed("Haval H6 Hybrid", "GWM", 243),
                new CarSeed("MG3 Hybrid+", "MG", 194)
        );

        List<RaceCar> cars = new ArrayList<>();
        for (int index = 0; index < users.size(); index++) {
            CarSeed seed = seeds.get(index);
            RaceCar car = new RaceCar();
            car.setName(seed.name());
            car.setBrand(seed.brand());
            car.setHorsePower(seed.horsePower());
            car.setOwner(users.get(index));
            car.setImagePosition("CENTER");
            cars.add(car);
        }
        return cars;
    }

    private List<Race> createRaces() {
        LocalDate today = LocalDate.now();
        return List.of(
                race("Negev Sunrise Sprint", "Sde Boker", today.plusDays(21), 24, RaceStatus.UPCOMING),
                race("Arava Hybrid Challenge", "Yotvata", today.plusDays(48), 30, RaceStatus.UPCOMING),
                race("Makhtesh Night Rally", "Mitzpe Ramon", today.plusDays(76), 20, RaceStatus.UPCOMING),
                race("Dead Sea Dust Run", "Ein Bokek", today.minusDays(35), 28, RaceStatus.PAST)
        );
    }

    private Race race(String name, String location, LocalDate date, int capacity, RaceStatus status) {
        Race race = new Race();
        race.setName(name);
        race.setLocation(location);
        race.setStartDate(date);
        race.setMaxParticipants(capacity);
        race.setStatus(status);
        return race;
    }

    private List<RaceRegistration> createRegistrations(
            List<User> users,
            List<RaceCar> cars,
            List<Race> races) {

        List<RaceRegistration> registrations = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            registrations.add(registration(users.get(index), cars.get(index), races.get(0)));
        }
        for (int index = 2; index < 10; index++) {
            registrations.add(registration(users.get(index), cars.get(index), races.get(1)));
        }
        for (int index = 0; index < 6; index++) {
            registrations.add(registration(users.get(index), cars.get(index), races.get(3)));
        }
        return registrations;
    }

    private RaceRegistration registration(User user, RaceCar car, Race race) {
        RaceRegistration registration = new RaceRegistration();
        registration.setUser(user);
        registration.setRaceCar(car);
        registration.setRace(race);
        return registration;
    }

    private List<DesertLiveItem> createPublications(
            User owner,
            List<User> users,
            List<Race> races) {

        Instant now = Instant.now();
        return List.of(
                publication(DesertLiveCategory.RACE, "Registration is open: Negev Sunrise Sprint", "Meet at dawn for a fast technical route around Sde Boker.", owner, races.get(0), 100, now),
                publication(DesertLiveCategory.RACE, "Arava Hybrid Challenge", "A long-distance event focused on hybrid efficiency and consistent pace.", owner, races.get(1), 95, now),
                publication(DesertLiveCategory.RACE, "Makhtesh Night Rally announced", "Night navigation, cool air and a dramatic Ramon Crater route.", owner, races.get(2), 90, now),
                publication(DesertLiveCategory.FESTIVAL, "Desert Lights Festival Camp", "Music, food trucks and a quiet family camping zone after the race.", users.get(2), null, 75, now),
                publication(DesertLiveCategory.COMMUNITY, "Volunteer recovery team meetup", "A practical meetup for safe towing, radios and desert first response.", users.get(3), null, 70, now),
                publication(DesertLiveCategory.MARKETPLACE, "Hybrid wheels and tires exchange", "Community swap for inspected wheels, tires and trail equipment.", users.get(10), null, 60, now),
                publication(DesertLiveCategory.NEWS, "Responsible driving in protected desert areas", "Stay on marked routes, carry water and leave every campsite clean.", users.get(11), null, 85, now),
                publication(DesertLiveCategory.COMMUNITY, "New drivers welcome evening", "An easy introduction to the club, its cars, races and festival weekends.", users.get(13), null, 65, now)
        );
    }

    private DesertLiveItem publication(
            DesertLiveCategory category,
            String title,
            String description,
            User author,
            Race linkedRace,
            int priority,
            Instant now) {

        DesertLiveItem item = new DesertLiveItem();
        item.setCategory(category);
        item.setSource(author.getRole() == Role.SUPER_ADMIN
                ? DesertLiveSource.SYSTEM
                : DesertLiveSource.USER);
        item.setModerationStatus(DesertLiveModerationStatus.APPROVED);
        item.setTitle(title);
        item.setDescription(description);
        item.setCreatedBy(author);
        item.setLinkedRace(linkedRace);
        item.setDisplayPriority(priority);
        item.setActiveFrom(now.minusSeconds(60));
        item.setActiveUntil(now.plusSeconds(60L * 60 * 24 * 180));
        if (author.getRole() != Role.SUPER_ADMIN) {
            item.setModeratedByUserId(null);
            item.setModerationNote("Approved demo publication");
            item.setModeratedAt(now);
        }
        return item;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    private record UserSeed(
            String name,
            int age,
            String location,
            String bio,
            String licenseCategory,
            MembershipTier membershipTier) {
    }

    private record CarSeed(String name, String brand, int horsePower) {
    }
}
