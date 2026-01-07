import java.util.*;
import java.io.*;
import java.util.UUID;
import java.text.SimpleDateFormat;

abstract class User {
    protected String username;
    public User(String username) { this.username = username; }
    public abstract void bookTicket(List<Theater> theaters);
    public abstract void cancelTicket(List<Theater> theaters);
}

class GuestUser extends User {
    public GuestUser(String username) { super(username); }
    public void bookTicket(List<Theater> theaters) { BookingHelper.book(theaters, username); }
    public void cancelTicket(List<Theater> theaters) { BookingHelper.cancel(theaters, username); }
}

class RegisteredUser extends User {
    private String password;
    public RegisteredUser(String username, String password) {
        super(username);
        this.password = password;
    }
    public boolean login(String enteredPassword) { return this.password.equals(enteredPassword); }
    public void bookTicket(List<Theater> theaters) { BookingHelper.book(theaters, username); }
    public void cancelTicket(List<Theater> theaters) { BookingHelper.cancel(theaters, username); }
}

class Theater {
    String name;
    List<Movie> movies;
    public Theater(String name) {
        this.name = name;
        this.movies = new ArrayList<>();
    }
    public void addMovie(Movie movie) { movies.add(movie); }
}

class Movie {
    String name;
    Map<String, boolean[][]> showtimes;
    public Movie(String name) {
        this.name = name;
        this.showtimes = new HashMap<>();
    }
    public void addShowtime(String time) {
        showtimes.put(time, new boolean[5][10]);
        for (int i = 0; i < 5; i++) Arrays.fill(showtimes.get(time)[i], true);
    }
}

class BookingHelper {
    static Scanner sc = new Scanner(System.in);

    public static void displaySeatGrid(boolean[][] seatGrid) {
        char rowChar = 'A';
        for (int i = 0; i < seatGrid.length; i++) {
            for (int j = 0; j < seatGrid[i].length; j++) {
                String seatLabel = "" + rowChar + (j + 1);
                System.out.print(seatGrid[i][j] ? seatLabel + " " : "[XX] ");
            }
            System.out.println();
            rowChar++;
        }
    }

    public static void book(List<Theater> theaters, String username) {
        System.out.println("Available Theaters:");
        for (int i = 0; i < theaters.size(); i++) {
            System.out.println((i + 1) + ". " + theaters.get(i).name);
        }
        System.out.print("Choose a theater: ");
        int theaterChoice = sc.nextInt();
        Theater selectedTheater = theaters.get(theaterChoice - 1);

        System.out.println("Available Movies:");
        for (int i = 0; i < selectedTheater.movies.size(); i++) {
            System.out.println((i + 1) + ". " + selectedTheater.movies.get(i).name);
        }
        System.out.print("Choose a movie: ");
        int movieChoice = sc.nextInt();
        Movie selectedMovie = selectedTheater.movies.get(movieChoice - 1);

        List<String> times = new ArrayList<>(selectedMovie.showtimes.keySet());
        for (int i = 0; i < times.size(); i++) {
            System.out.println((i + 1) + ". " + times.get(i));
        }
        System.out.print("Choose showtime: ");
        int timeChoice = sc.nextInt();
        String time = times.get(timeChoice - 1);
        boolean[][] seatGrid = selectedMovie.showtimes.get(time);

        System.out.println("\nAvailable Seats:");
        displaySeatGrid(seatGrid);

        System.out.print("How many tickets (max 6): ");
        int ticketCount = sc.nextInt();
        if (ticketCount < 1 || ticketCount > 6) {
            System.out.println("Invalid number of tickets.");
            return;
        }

        List<String> selectedSeats = new ArrayList<>();
        for (int i = 0; i < ticketCount; i++) {
            while (true) {
                System.out.print("Enter seat (e.g., A1): ");
                String input = sc.next().toUpperCase();
                if (input.length() < 2 || input.length() > 3) continue;
                char rowChar = input.charAt(0);
                int row = rowChar - 'A';
                int col;
                try {
                    col = Integer.parseInt(input.substring(1)) - 1;
                } catch (NumberFormatException e) {
                    continue;
                }
                if (row < 0 || row >= 5 || col < 0 || col >= 10 || !seatGrid[row][col]) {
                    System.out.println("Invalid or already booked seat. Try another.");
                    continue;
                }
                seatGrid[row][col] = false;
                selectedSeats.add(input);
                break;
            }
        }

        int money = ticketCount * 120;
        System.out.println("Total Amount: Rs." + money + "\nSelect UPI App:\n1. Google Pay\n2. PhonePe\n3. Paytm");
	System.out.println("Enter choice:");
        int appChoice = sc.nextInt();
        String app = "Unknown";
        if (appChoice == 1) app = "Google Pay";
        else if (appChoice == 2) app = "PhonePe";
        else if (appChoice == 3) app = "Paytm";
        System.out.print("Enter UPI PIN to pay: ");
        if (!sc.next().equals("1234")) {
            System.out.println("Invalid PIN.");
            return;
        }

        String ticketID = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
        System.out.println("================================================================");
        String ticket = "Ticket ID: " + ticketID +
                        "\nUsername: " + username +
                        "\nTheater: " + selectedTheater.name +
                        "\nMovie: " + selectedMovie.name +
                        "\nTime: " + time +
                        "\nSeats: " + selectedSeats +
                        "\nPayment: Rs." + money + " via " + app;
        System.out.println("=================================================================");
        try (FileWriter fw = new FileWriter(ticketID + ".txt")) {
            fw.write(ticket);
            System.out.println("\nTicket booked successfully!\n" + ticket);
        } catch (IOException e) {
            System.out.println("Failed to save ticket.");
        }
    }

    public static void cancel(List<Theater> theaters, String username) {
    System.out.print("Enter Ticket ID to cancel: ");
    String ticketID = sc.next();
    File file = new File(ticketID + ".txt");
    if (!file.exists()) {
        System.out.println("Ticket not found.");
        return;
    }
    try {
        List<String> lines = new ArrayList<>();
        Scanner fileScanner = new Scanner(file, "UTF-8");
        while (fileScanner.hasNextLine()) lines.add(fileScanner.nextLine());
        fileScanner.close();

        String theaterName = "", movieName = "", showtime = "", seatLine = "";
        for (String line : lines) {
            if (line.startsWith("Theater: ")) theaterName = line.substring(9);
            if (line.startsWith("Movie: ")) movieName = line.substring(7);
            if (line.startsWith("Time: ")) showtime = line.substring(6);
            if (line.startsWith("Seats: ")) seatLine = line.substring(7);
        }

        List<String> seats = new ArrayList<>(Arrays.asList(seatLine.replace("[", "").replace("]", "").split(",\\s*")));
        System.out.println("Your Booked Seats: " + seats);
        System.out.print("Enter seats to cancel (comma separated, e.g., A1,A2): ");
        String input = sc.next().toUpperCase();
        String[] seatsToCancel = input.split(",");
        List<String> cancelled = new ArrayList<>();

        Theater theater = null;
        for (Theater t : theaters) if (t.name.equals(theaterName)) theater = t;
        Movie movie = null;
        for (Movie m : theater.movies) if (m.name.equals(movieName)) movie = m;
        boolean[][] seatGrid = movie.showtimes.get(showtime);

        for (String seat : seatsToCancel) {
            seat = seat.trim();
            if (seats.contains(seat)) {
                int row = seat.charAt(0) - 'A';
                int col = Integer.parseInt(seat.substring(1)) - 1;
                seatGrid[row][col] = true;
                seats.remove(seat);
                cancelled.add(seat);
            } else {
                System.out.println("Seat " + seat + " not found in ticket.");
            }
        }

        if (seats.isEmpty()) file.delete();
        else {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            for (String line : lines) {
                if (line.startsWith("Seats: ")) writer.write("Seats: " + seats + "\n");
                else writer.write(line + "\n");
            }
            writer.close();
        }

        BufferedWriter log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("refund_log.txt", true), "UTF-8"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (String seat : cancelled) {
            log.write("Ticket ID: " + ticketID + ", Username: " + username + ", Seat: " + seat +
                    ", Refund: 120, DateTime: " + sdf.format(new Date()) + "\n");
        }
        log.close();

        System.out.println("Cancelled seats: " + cancelled + ". Total Refund: " + (cancelled.size() * 120));
    } catch (Exception e) {
        System.out.println("Error during cancellation: " + e.getMessage());
    }
}


    public static void viewTicket() {
        System.out.print("Enter Ticket ID to view: ");
        File file = new File(sc.next() + ".txt");
        if (file.exists()) {
            try {
                Scanner fileScanner = new Scanner(file);
                while (fileScanner.hasNextLine()) System.out.println(fileScanner.nextLine());
                fileScanner.close();
            } catch (Exception e) {
                System.out.println("Error reading ticket.");
            }
        } else System.out.println("Ticket not found.");
    }

    public static void viewRefundLog() {
        System.out.println("=============== Refund Log ===============");
        File file = new File("refund_log.txt");
        if (!file.exists()) {
            System.out.println("No refund history found.");
            return;
        }

        int totalRefund = 0;
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
                
                if (line.contains("Refund: ")) {
                    String[] parts = line.split("Refund: ");
                    if (parts.length > 1) {
                        String amountPart = parts[1].split(",")[0].trim();
                        String digits = amountPart.replaceAll("[^0-9]", "");

                        if (!digits.isEmpty()) {
                            totalRefund += Integer.parseInt(digits);
                        }
                    }
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Error reading refund log.");
        }

        System.out.println("******************************************");
        System.out.println("Total Refund Amount: " + totalRefund);
        System.out.println("==========================================");
    }


}
 class MovieBookingApp {
    static Scanner sc = new Scanner(System.in);
    static List<Theater> theaters = new ArrayList<>();
    static Map<String, RegisteredUser> registeredUsers = new HashMap<>();

    public static void main(String[] args) {
        setupTheaters();

        User currentUser = null;

        while (true) {
            System.out.println("\n============= Movie Ticket Booking System==================");
            System.out.println("1. Login as Registered User");
            System.out.println("2. Continue as Guest");
            System.out.println("3. Register New User");
            System.out.println("4. Exit");
	    System.out.println("\n===============================================================");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    currentUser = loginUser();
                    break;
                case 2:
                    System.out.print("Enter guest username: ");
                    currentUser = new GuestUser(sc.next());
                    break;
                case 3:
                    registerUser();
                    break;
                case 4:
                    System.out.println("Thank you! Goodbye.");
                    return;
                default:
                    System.out.println("Invalid choice.");
                    continue;
            }

            if (currentUser != null) {
                while (true) {
                    System.out.println("\n========================Main Menu =====================================");
                    System.out.println("1. Book Ticket");
                    System.out.println("2. Cancel Ticket Seat");
                    System.out.println("3. View Ticket");
                    System.out.println("4. View Refund Log");
                    System.out.println("5. Logout");
		    System.out.println("\n=========================================================================");
                    System.out.print("Enter choice: ");
                    int menuChoice = sc.nextInt();

                    switch (menuChoice) {
                        case 1:
                            currentUser.bookTicket(theaters);
                            break;
                        case 2:
                            currentUser.cancelTicket(theaters);
                            break;
                        case 3:
                            BookingHelper.viewTicket();
                            break;
                        case 4:
                            BookingHelper.viewRefundLog();
                            break;
                        case 5:
                            currentUser = null;
                            System.out.println("Logged out.");
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }

                    if (currentUser == null) break;
                }
            }
        }
    }

   private static void setupTheaters() {
    Theater t1 = new Theater("PVR Panjagutta");
    Movie m1 = new Movie("Kalki 2898 AD");
    m1.addShowtime("9:00 AM");
    m1.addShowtime("1:00 PM");
    m1.addShowtime("6:00 PM");
    m1.addShowtime("9:30 PM");

    Movie m2 = new Movie("Pushpa 2: The Rule");
    m2.addShowtime("10:00 AM");
    m2.addShowtime("2:00 PM");
    m2.addShowtime("7:00 PM");

    t1.addMovie(m1);
    t1.addMovie(m2);

    Theater t2 = new Theater("Asian M Cube Mall");
    Movie m3 = new Movie("Hanuman");
    m3.addShowtime("11:00 AM");
    m3.addShowtime("3:00 PM");
    m3.addShowtime("8:00 PM");

    Movie m4 = new Movie("Guntur Kaaram");
    m4.addShowtime("12:00 PM");
    m4.addShowtime("4:30 PM");
    m4.addShowtime("10:00 PM");

    t2.addMovie(m3);
    t2.addMovie(m4);

    Theater t3 = new Theater("INOX GVK One");
    Movie m5 = new Movie("Pushpa 2: The Rule");
    m5.addShowtime("9:45 AM");
    m5.addShowtime("1:45 PM");
    m5.addShowtime("6:30 PM");

    Movie m6 = new Movie("Kalki 2898 AD");
    m6.addShowtime("11:00 AM");
    m6.addShowtime("5:00 PM");
    m6.addShowtime("9:00 PM");

    t3.addMovie(m5);
    t3.addMovie(m6);

    Theater t4 = new Theater("AMB Cinemas");
    Movie m7 = new Movie("Hanuman");
    m7.addShowtime("10:15 AM");
    m7.addShowtime("2:15 PM");
    m7.addShowtime("7:30 PM");

    Movie m8 = new Movie("Guntur Kaaram");
    m8.addShowtime("9:00 AM");
    m8.addShowtime("1:00 PM");
    m8.addShowtime("6:00 PM");
    m8.addShowtime("10:00 PM");

    t4.addMovie(m7);
    t4.addMovie(m8);

    // Add all to main list
    theaters.add(t1);
    theaters.add(t2);
    theaters.add(t3);
    theaters.add(t4);
}


    private static void registerUser() {
        System.out.print("Enter new username: ");
        String username = sc.next();
        if (registeredUsers.containsKey(username)) {
            System.out.println("Username already exists.");
            return;
        }
        System.out.print("Enter password: ");
        String password = sc.next();
        registeredUsers.put(username, new RegisteredUser(username, password));
        System.out.println("User registered successfully.");
    }

    private static User loginUser() {
        System.out.print("Enter username: ");
        String username = sc.next();
        if (!registeredUsers.containsKey(username)) {
            System.out.println("User not found.");
            return null;
        }
        System.out.print("Enter password: ");
        String password = sc.next();
        RegisteredUser user = registeredUsers.get(username);
        if (user.login(password)) {
            System.out.println("Login successful.");
            return user;
        } else {
            System.out.println("Incorrect password.");
            return null;
        }
    }
}
