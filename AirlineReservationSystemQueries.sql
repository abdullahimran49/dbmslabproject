CREATE DATABASE AirlineReservationSystem;
GO

USE AirlineReservationSystem;
GO

Alter TABLE Users (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    FullName VARCHAR(100) NOT NULL,
    CNIC VARCHAR(13) UNIQUE NOT NULL,
    Email VARCHAR(100) NOT NULL,
    Password VARCHAR(100) NOT NULL,
    Phone VARCHAR(20) NOT NULL,
    Role VARCHAR(10) CHECK (Role IN ('user', 'admin','superadmin')),
);


SELECT name
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('Users') AND definition LIKE '%Role%';

ALTER TABLE Users
DROP CONSTRAINT CK_Users_Role;

ALTER TABLE Users
ADD CONSTRAINT CK_Users_Role CHECK (Role IN ('user', 'admin', 'superadmin'));

select * from Users

select * from users
CREATE TABLE Airlines (
    AirlineID INT PRIMARY KEY IDENTITY(1,1),
    Name VARCHAR(100) NOT NULL,
    Country VARCHAR(50)
);

CREATE TABLE Flights (
    FlightID INT PRIMARY KEY IDENTITY(1,1),
    AirlineID INT,
    FlightNumber VARCHAR(20) NOT NULL,
    Origin VARCHAR(100) NOT NULL,
    Destination VARCHAR(100) NOT NULL,
    DepartureTime DATETIME NOT NULL,
    ArrivalTime DATETIME NOT NULL,
    Duration VARCHAR(20),
    Price DECIMAL(10,2) NOT NULL,
    Status VARCHAR(20) CHECK (Status IN ('Scheduled', 'Cancelled', 'Delayed')),
    FOREIGN KEY (AirlineID) REFERENCES Airlines(AirlineID)
);

CREATE TABLE Seats (
    SeatID INT PRIMARY KEY IDENTITY(1,1),
    FlightID INT,
    SeatNumber VARCHAR(10) NOT NULL,
    Class VARCHAR(20) CHECK (Class IN ('Economy', 'Business')),
    IsAvailable BIT DEFAULT 1,
    FOREIGN KEY (FlightID) REFERENCES Flights(FlightID)
);


CREATE TABLE Bookings (
    BookingID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT,
    FlightID INT,
    BookingDate DATETIME DEFAULT GETDATE(),
    TotalAmount DECIMAL(10,2),
    Status VARCHAR(20) CHECK (Status IN ('Confirmed', 'Cancelled', 'Pending')),
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (FlightID) REFERENCES Flights(FlightID)
);

CREATE TABLE BookedSeats (
    BookedSeatID INT PRIMARY KEY IDENTITY(1,1),
    BookingID INT,
    SeatID INT,
    FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID),
    FOREIGN KEY (SeatID) REFERENCES Seats(SeatID)
);


CREATE TABLE PaymentMethods (
    PaymentMethodID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT,
    CardNumber VARCHAR(20) NOT NULL,
    CardType VARCHAR(20),
    ExpiryDate DATE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);


CREATE TABLE Payments (
    PaymentID INT PRIMARY KEY IDENTITY(1,1),
    BookingID INT,
    PaymentMethodID INT,
    Amount DECIMAL(10,2),
    PaymentDate DATETIME DEFAULT GETDATE(),
    Status VARCHAR(20) CHECK (Status IN ('Paid', 'Failed')),
    FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID),
    FOREIGN KEY (PaymentMethodID) REFERENCES PaymentMethods(PaymentMethodID)
);




Use AirlineReservationSystem

 ALTER PROCEDURE RequestRefund
    @BookingID INT,
    @Success BIT OUTPUT
AS
BEGIN
    IF EXISTS (
        SELECT 1 FROM Bookings
        WHERE BookingID = @BookingID AND Status = 'Confirmed'
    )
    BEGIN
        UPDATE Bookings
        SET Status = 'Cancelled'
        WHERE BookingID = @BookingID;

        SET @Success = 1;
    END
    ELSE
    BEGIN
        SET @Success = 0;
    END
END

select * from PaymentMethods
select * from flights

CREATE PROCEDURE sp_UpdateUserRole
    @UserID INT,
    @FullName NVARCHAR(100),
    @CNIC VARCHAR(13),
    @Email VARCHAR(100),
    @Phone VARCHAR(11),
    @Role VARCHAR(20)
AS
BEGIN
    UPDATE Users
    SET FullName = @FullName,
        CNIC = @CNIC,
        Email = @Email,
        Phone = @Phone,
        Role = @Role
    WHERE UserID = @UserID;
END

CREATE PROCEDURE GetPaymentHistoryByUserId
    @UserId INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        p.PaymentID,
        p.BookingID,
        f.FlightNumber,
        CONCAT(f.Origin, ' to ', f.Destination) AS Route,
        p.PaymentDate,
        p.Amount,
        p.Status
    FROM Payments p
    INNER JOIN Bookings b ON p.BookingID = b.BookingID
    INNER JOIN Flights f ON b.FlightID = f.FlightID
    WHERE b.UserID = @UserId
    ORDER BY p.PaymentDate DESC;
END;

CREATE PROCEDURE RequestRefund
    @BookingID INT,
    @Success BIT OUTPUT
AS
BEGIN
    DECLARE @BookingDate DATETIME;

    SELECT @BookingDate = BookingDate FROM Bookings WHERE BookingID = @BookingID;

    IF @BookingDate IS NULL
    BEGIN
        SET @Success = 0;
        RETURN;
    END

    -- Check if it's been LESS than or equal to 24 hours since booking
    IF DATEDIFF(HOUR, @BookingDate, GETDATE()) <= 24
    BEGIN
        UPDATE Bookings
        SET Status = 'Refunded'
        WHERE BookingID = @BookingID;

        SET @Success = 1;
    END
    ELSE
    BEGIN
        SET @Success = 0;
    END
END

 ALTER PROCEDURE RequestRefund
    @BookingID INT,
    @Success BIT OUTPUT
AS
BEGIN
    -- Only allow refund if booking is currently 'Confirmed'
    IF EXISTS (
        SELECT 1 FROM Bookings
        WHERE BookingID = @BookingID AND Status = 'Confirmed'
    )
    BEGIN
        UPDATE Bookings
        SET Status = 'Cancelled'
        WHERE BookingID = @BookingID;

        SET @Success = 1;
    END
    ELSE
    BEGIN
        SET @Success = 0;
    END
END

select * from Seats
select * from BookedSeats

CREATE PROCEDURE CancelBooking
    @BookingID INT
AS
BEGIN
    UPDATE Bookings
    SET Status = 'Cancelled'
    WHERE BookingID = @BookingID;

    UPDATE Seats
    SET IsAvailable = 1
    WHERE SeatID IN (
        SELECT SeatID
        FROM BookedSeats
        WHERE BookingID = @BookingID
    );
END

DROP PROCEDURE CancelBooking

SELECT TOP (1000) [PaymentMethodID]
      ,[UserID]
      ,[CardNumber]
      ,[CardType]
      ,[ExpiryDate]
  FROM [AirlineReservationSystem].[dbo].[PaymentMethods]


  select * from Users

  Use AirlineReservationSystem;

CREATE PROCEDURE ValidateUser
    @Email VARCHAR(255),
    @Password VARCHAR(255)
AS
BEGIN
    SELECT * FROM Users WHERE Email = @Email AND Password = @Password;
END;


CREATE PROCEDURE InsertUser
    @FullName VARCHAR(255),
    @CNIC VARCHAR(13),
    @Email VARCHAR(255),
    @Password VARCHAR(255),
    @Phone VARCHAR(20),
    @Role VARCHAR(50)
AS
BEGIN
    INSERT INTO Users (FullName, CNIC, Email, Password, Phone, Role)
    VALUES (@FullName, @CNIC, @Email, @Password, @Phone, @Role);
END;


CREATE PROCEDURE CheckUserExists
    @cnic VARCHAR(13),
    @Email VARCHAR(255)
AS
BEGIN
    SELECT * FROM Users WHERE CNIC = @cnic OR Email = @Email;
END;

CREATE PROCEDURE GetSeatsByFlightID
    @FlightID INT
AS
BEGIN
    SELECT SeatNumber, Class, IsAvailable
    FROM Seats
    WHERE FlightID = @FlightID;
END;

Use AirlineReservationSystem

CREATE PROCEDURE sp_AddAirline
    @Name VARCHAR(100),
    @Country VARCHAR(50)
AS
BEGIN
    INSERT INTO Airlines (Name, Country)
    VALUES (@Name, @Country);
END;

CREATE PROCEDURE sp_GetAllAirlines
AS
BEGIN
    SELECT AirlineID, Name, Country
    FROM Airlines
    ORDER BY AirlineID;
END;

CREATE PROCEDURE sp_DeleteAirline
    @AirlineID INT
AS
BEGIN
    DELETE FROM Airlines
    WHERE AirlineID = @AirlineID;
END;


ALTER PROCEDURE ResetUserPassword
    @UserID INT,
    @OldPassword VARCHAR(100),
    @NewPassword VARCHAR(100),
    @StatusCode INT OUTPUT,
    @Message NVARCHAR(200) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (
        SELECT 1 FROM Users WHERE UserID = @UserID AND Password = @OldPassword
    )
    BEGIN
        SET @StatusCode = 0;
        SET @Message = 'Old password is incorrect.';
        RETURN;
    END

    UPDATE Users
    SET Password = @NewPassword
    WHERE UserID = @UserID;

    SET @StatusCode = 1;
    SET @Message = 'Password updated successfully.';
END;
GO

CREATE PROCEDURE sp_CheckUserDuplicate
    @Email NVARCHAR(100),
    @CNIC NVARCHAR(20)
AS
BEGIN
    SELECT COUNT(*) AS DuplicateCount
    FROM Users
    WHERE Email = @Email OR CNIC = @CNIC;
END;

Alter PROCEDURE CheckUserExists
    @cnic NVARCHAR(13),
    @email NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @cnicExists BIT = 0;
    DECLARE @emailExists BIT = 0;

    IF EXISTS (SELECT 1 FROM Users WHERE CNIC = @cnic)
        SET @cnicExists = 1;

    IF EXISTS (SELECT 1 FROM Users WHERE Email = @email)
        SET @emailExists = 1;

    SELECT @cnicExists AS cnicExists, @emailExists AS emailExists;
END;

Select * from users

ALTER PROCEDURE CheckUserExists
    @cnic NVARCHAR(13),
    @email NVARCHAR(100),
    @phone NVARCHAR(11)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @emailExists BIT = 0;
    DECLARE @phoneExists BIT = 0;

    -- Only check if other users (different CNIC) have same email or phone
    IF EXISTS (SELECT 1 FROM Users WHERE Email = @email AND CNIC <> @cnic)
        SET @emailExists = 1;

    IF EXISTS (SELECT 1 FROM Users WHERE Phone = @phone AND CNIC <> @cnic)
        SET @phoneExists = 1;

    SELECT 
        @emailExists AS emailExists, 
        @phoneExists AS phoneExists;
END;


select * from Bookings

CREATE PROCEDURE GetUserPayments
    @UserID INT
AS
BEGIN
    SELECT 
        p.PaymentID, 
        p.BookingID, 
        f.FlightNumber, 
        CONCAT(f.Origin, ' to ', f.Destination) AS Route, 
        p.PaymentDate, 
        p.Amount, 
        p.Status
    FROM Payments p
    JOIN Bookings b ON p.BookingID = b.BookingID
    JOIN Flights f ON b.FlightID = f.FlightID
    WHERE b.UserID = @UserID
    ORDER BY p.PaymentDate DESC;
END;

CREATE PROCEDURE CheckSeat
    @FlightID INT,
    @SeatNumber VARCHAR(10)
AS
BEGIN
    SELECT IsAvailable
    FROM Seats
    WHERE FlightID = @FlightID AND SeatNumber = @SeatNumber;
END;

CREATE PROCEDURE GetPassengerName
    @UserID INT
AS
BEGIN
    SELECT FullName
    FROM Users
    WHERE UserID = @UserID;
END;

CREATE PROCEDURE InsertPendingBooking
    @UserID INT,
    @FlightID INT
AS
BEGIN
    INSERT INTO Bookings (UserID, FlightID, BookingDate, Status)
    VALUES (@UserID, @FlightID, GETDATE(), 'Pending');

    SELECT SCOPE_IDENTITY() AS BookingID;
END;

CREATE PROCEDURE CheckBookingStatus
    @BookingID INT
AS
BEGIN
    SELECT Status FROM Bookings WHERE BookingID = @BookingID;
END;

CREATE PROCEDURE GetFlightPrice
    @FlightID INT
AS
BEGIN
    SELECT Price FROM Flights WHERE FlightID = @FlightID;
END;

select * from users

ALTER PROCEDURE CheckUserExists
    @userID INT,
    @email NVARCHAR(100),
    @phone NVARCHAR(11)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @emailExists BIT = 0;
    DECLARE @phoneExists BIT = 0;

    -- Check if email exists for a different user
    IF EXISTS (SELECT 1 FROM Users WHERE Email = @email AND UserID <> @userID)
        SET @emailExists = 1;

    -- Check if phone exists for a different user
    IF EXISTS (SELECT 1 FROM Users WHERE Phone = @phone AND UserID <> @userID)
        SET @phoneExists = 1;

    SELECT 
        @emailExists AS emailExists, 
        @phoneExists AS phoneExists;
END;


use AirlineReservationSystem
GO



--Booking Management

CREATE PROCEDURE ShowAllBookings
AS
BEGIN
    SELECT 
        b.BookingID,
        u.FullName,
        u.Email,
        f.FlightID,
        f.FlightNumber,
        f.Origin,
        f.Destination,
        f.DepartureTime,
        f.ArrivalTime,
        b.BookingDate,
        b.TotalAmount,
        b.Status
    FROM Bookings b
    JOIN Users u ON b.UserID = u.UserID
    JOIN Flights f ON b.FlightID = f.FlightID
    ORDER BY b.BookingDate DESC;
END


CREATE PROCEDURE CancelBooking
    @BookingID INT
AS
BEGIN
    UPDATE Bookings
    SET Status = 'Cancelled'
    WHERE BookingID = @BookingID;
END


CREATE PROCEDURE ConfirmBooking
    @BookingID INT
AS
BEGIN
    UPDATE Bookings
    SET Status = 'Confirmed'
    WHERE BookingID = @BookingID AND Status <> 'Cancelled' AND Status <> 'Confirmed';
    
    -- Return number of rows affected
    SELECT @@ROWCOUNT AS RowsAffected;
END







--Flight Management

CREATE PROCEDURE UpdateFlight
    @FlightID INT,
    @AirlineID INT,
    @FlightNumber VARCHAR(20),
    @Origin VARCHAR(100),
    @Destination VARCHAR(100),
    @DepartureTime DATETIME,
    @ArrivalTime DATETIME,
    @Duration VARCHAR(20),
    @Price DECIMAL(10,2),
    @Status VARCHAR(20)
AS
BEGIN
    UPDATE Flights
    SET AirlineID = @AirlineID,
        FlightNumber = @FlightNumber,
        Origin = @Origin,
        Destination = @Destination,
        DepartureTime = @DepartureTime,
        ArrivalTime = @ArrivalTime,
        Duration = @Duration,
        Price = @Price,
        Status = @Status
    WHERE FlightID = @FlightID
END
GO

CREATE PROCEDURE CancelFlight
    @FlightID INT
AS
BEGIN
    UPDATE Flights
    SET Status = 'Cancelled'
    WHERE FlightID = @FlightID;
END


CREATE PROCEDURE ShowAllFlights
AS
BEGIN
    SELECT
        f.FlightID,
        a.Name AS AirlineName,
        f.FlightNumber,
        f.Origin,
        f.Destination,
        f.DepartureTime,
        f.ArrivalTime,
        f.Duration,
        f.Price,
        f.Status
    FROM Flights f
    JOIN Airlines a ON f.AirlineID = a.AirlineID
    ORDER BY f.DepartureTime ASC;
END


CREATE PROCEDURE GetAirlineIDByName
    @AirlineName NVARCHAR(100)
AS
BEGIN
    SELECT AirlineID FROM Airlines WHERE Name = @AirlineName;
END


-- Add Flight
CREATE PROCEDURE AddFlight
    @AirlineID INT,
    @FlightNumber VARCHAR(20),
    @Origin VARCHAR(100),
    @Destination VARCHAR(100),
    @DepartureTime DATETIME,
    @ArrivalTime DATETIME,
    @Duration VARCHAR(20),
    @Price DECIMAL(10,2),
    @Status VARCHAR(20)
AS
BEGIN
    INSERT INTO Flights (AirlineID, FlightNumber, Origin, Destination, DepartureTime, ArrivalTime, Duration, Price, Status)
    VALUES (@AirlineID, @FlightNumber, @Origin, @Destination, @DepartureTime, @ArrivalTime, @Duration, @Price, @Status)
END
GO

CREATE PROCEDURE AddFlightWithSeats
    @AirlineID INT,
    @FlightNumber VARCHAR(10),
    @Origin VARCHAR(100),
    @Destination VARCHAR(100),
    @DepartureTime VARCHAR(50),
    @ArrivalTime VARCHAR(50),
    @Duration VARCHAR(20),
    @Price DECIMAL(10,2),
    @Status VARCHAR(20),
    @EconomySeats INT,
    @BusinessSeats INT
AS
BEGIN
    DECLARE @FlightID INT;
    
    -- Insert flight
    INSERT INTO Flights (AirlineID, FlightNumber, Origin, Destination, DepartureTime, ArrivalTime, Duration, Price, Status)
    VALUES (@AirlineID, @FlightNumber, @Origin, @Destination, @DepartureTime, @ArrivalTime, @Duration, @Price, @Status);
    
    -- Get flight ID
    SET @FlightID = SCOPE_IDENTITY();
    
    -- Add economy seats
    DECLARE @i INT = 1;
    WHILE @i <= @EconomySeats
    BEGIN
        INSERT INTO Seats (FlightID, SeatNumber, Class, IsAvailable)
        VALUES (@FlightID, 'E' + CAST(@i AS VARCHAR), 'Economy', 1);
        SET @i = @i + 1;
    END
    
    -- Add business seats
    SET @i = 1;
    WHILE @i <= @BusinessSeats
    BEGIN
        INSERT INTO Seats (FlightID, SeatNumber, Class, IsAvailable)
        VALUES (@FlightID, 'B' + CAST(@i AS VARCHAR), 'Business', 1);
        SET @i = @i + 1;
    END
END

CREATE PROCEDURE sp_GetAirlines
AS
BEGIN
    SELECT AirlineID, Name FROM Airlines;
END;




--User management
CREATE PROCEDURE sp_ViewAllUsers
AS
BEGIN
    SELECT 
        UserID, 
        FullName, 
        CNIC, 
        Email, 
        Phone, 
        Role 
    FROM dbo.Users
END



CREATE PROCEDURE sp_UpdateUser
    @UserID INT,
    @FullName NVARCHAR(100),
    @Email NVARCHAR(100),
    @Phone NVARCHAR(20),
    @Role NVARCHAR(10)
AS
BEGIN
    UPDATE dbo.Users
    SET 
        FullName = @FullName, 
        Email = @Email, 
        Phone = @Phone, 
        Role = @Role
    WHERE UserID = @UserID
END


CREATE PROCEDURE sp_DeleteUser
    @UserID INT
AS
BEGIN
    DELETE FROM dbo.Users WHERE UserID = @UserID
END


select * from bookedseats
select * from users

UPDATE bookings
SET Status = 'Pending'
WHERE BookingID = 3;

DROP PROCEDURE AddSeat
    @FlightID INT,
    @SeatNumber VARCHAR(10),
    @Class VARCHAR(20),
    @IsAvailable BIT
AS
BEGIN
    INSERT INTO Seats (FlightID, SeatNumber, Class, IsAvailable)
    VALUES (@FlightID, @SeatNumber, @Class, @IsAvailable)
END



--super user management

CREATE PROCEDURE sp_GetUsers
AS
BEGIN
    SELECT UserID, FullName, CNIC, Email, Phone, Role FROM Users;
END;





-- Stored Procedures for PaymentDialogue functionality (No Transactions)

-- 1. Get Payment Methods for User
CREATE PROCEDURE GetUserPaymentMethods
    @UserID INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT PaymentMethodID, CardNumber, CardType, ExpiryDate 
    FROM PaymentMethods 
    WHERE UserID = @UserID;
END;

-- 2. Get Flight Info from Booking
CREATE PROCEDURE GetBookingFlightInfo
    @BookingID INT,
    @FlightID INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT @FlightID = FlightID 
    FROM Bookings 
    WHERE BookingID = @BookingID;
    
    IF @FlightID IS NULL
        SET @FlightID = -1;
END;

-- 3. Check Seat Availability
CREATE PROCEDURE CheckSeatAvailability
    @FlightID INT,
    @SeatNumber VARCHAR(10),
    @IsAvailable BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT @IsAvailable = IsAvailable 
    FROM Seats 
    WHERE FlightID = @FlightID AND SeatNumber = @SeatNumber;
    
    IF @IsAvailable IS NULL
        SET @IsAvailable = 0;
END;

-- 4. Record Payment
CREATE PROCEDURE RecordPayment
    @BookingID INT,
    @PaymentMethodID INT,
    @Amount DECIMAL(10,2),
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        INSERT INTO Payments (BookingID, PaymentMethodID, Amount, Status) 
        VALUES (@BookingID, @PaymentMethodID, @Amount, 'Paid');
        
        SET @Success = 1;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;

-- 5. Update Booking Amount
create PROCEDURE UpdateBookingAmount
    @BookingID INT,
    @Amount DECIMAL(10,2),
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        UPDATE Bookings 
        SET TotalAmount = @Amount 
        WHERE BookingID = @BookingID;
        
        IF @@ROWCOUNT > 0
            SET @Success = 1;
        ELSE
            SET @Success = 0;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;

-- 6. Reserve Seat
CREATE PROCEDURE ReserveSeat
    @FlightID INT,
    @SeatNumber VARCHAR(10),
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        UPDATE Seats 
        SET IsAvailable = 0 
        WHERE FlightID = @FlightID AND SeatNumber = @SeatNumber AND IsAvailable = 1;
        
        IF @@ROWCOUNT > 0
            SET @Success = 1;
        ELSE
            SET @Success = 0;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;

-- 7. Get Seat ID
CREATE PROCEDURE GetSeatID
    @FlightID INT,
    @SeatNumber VARCHAR(10),
    @SeatID INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT @SeatID = SeatID 
    FROM Seats 
    WHERE FlightID = @FlightID AND SeatNumber = @SeatNumber;
    
    IF @SeatID IS NULL
        SET @SeatID = -1;
END;

-- 8. Link Seat to Booking
CREATE PROCEDURE LinkSeatToBooking
    @BookingID INT,
    @SeatID INT,
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        INSERT INTO BookedSeats (BookingID, SeatID) 
        VALUES (@BookingID, @SeatID);
        
        SET @Success = 1;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;

-- 9. Update Booking Status
CREATE PROCEDURE UpdateBookingStatus
    @BookingID INT,
    @Status VARCHAR(20),
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        UPDATE Bookings 
        SET Status = @Status 
        WHERE BookingID = @BookingID;
        
        IF @@ROWCOUNT > 0
            SET @Success = 1;
        ELSE
            SET @Success = 0;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;

-- 10. Get Latest Payment Method ID
CREATE PROCEDURE GetLatestPaymentMethodID
    @UserID INT,
    @CardNumber VARCHAR(20),
    @PaymentMethodID INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT TOP 1 @PaymentMethodID = PaymentMethodID 
    FROM PaymentMethods 
    WHERE UserID = @UserID AND CardNumber = @CardNumber 
    ORDER BY PaymentMethodID DESC;
    
    IF @PaymentMethodID IS NULL
        SET @PaymentMethodID = -1;
END;

-- 11. Rollback Seat Reservation (in case of failure)
CREATE PROCEDURE RollbackSeatReservation
    @FlightID INT,
    @SeatNumber VARCHAR(10),
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        UPDATE Seats 
        SET IsAvailable = 1 
        WHERE FlightID = @FlightID AND SeatNumber = @SeatNumber;
        
        IF @@ROWCOUNT > 0
            SET @Success = 1;
        ELSE
            SET @Success = 0;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;

-- 12. Remove Booked Seat Link (in case of failure)
CREATE PROCEDURE RemoveBookedSeatLink
    @BookingID INT,
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        DELETE FROM BookedSeats 
        WHERE BookingID = @BookingID;
        
        SET @Success = 1;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;

-- 13. Remove Payment Record (in case of failure)
CREATE PROCEDURE RemovePaymentRecord
    @BookingID INT,
    @Success BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        DELETE FROM Payments 
        WHERE BookingID = @BookingID;
        
        SET @Success = 1;
    END TRY
    BEGIN CATCH
        SET @Success = 0;
    END CATCH;
END;


Use AirlineReservationSystem

ALTER PROCEDURE UpdateUserProfile
    @UserID INT,
    @FullName VARCHAR(100),
    @Email VARCHAR(100),
    @Password VARCHAR(100),
    @Phone VARCHAR(20),
    @CNIC VARCHAR(20),
    @Role VARCHAR(20)
AS
BEGIN
    UPDATE Users
    SET FullName = @FullName,
        Email = @Email,
        Password = @Password,
        Phone = @Phone,
        CNIC = @CNIC,
        Role = @Role
    WHERE UserID = @UserID;
END



use AirlineReservationSystem
GO



--Booking Management

CREATE PROCEDURE ShowAllBookings
AS
BEGIN
    SELECT 
        b.BookingID,
        u.FullName,
        u.Email,
        f.FlightID,
        f.FlightNumber,
        f.Origin,
        f.Destination,
        f.DepartureTime,
        f.ArrivalTime,
        b.BookingDate,
        b.TotalAmount,
        b.Status
    FROM Bookings b
    JOIN Users u ON b.UserID = u.UserID
    JOIN Flights f ON b.FlightID = f.FlightID
    ORDER BY b.BookingDate DESC;
END
go

CREATE PROCEDURE CancelBooking
    @BookingID INT
AS
BEGIN
    UPDATE Bookings
    SET Status = 'Cancelled'
    WHERE BookingID = @BookingID;
END


CREATE PROCEDURE ConfirmBooking
    @BookingID INT
AS
BEGIN
    UPDATE Bookings
    SET Status = 'Confirmed'
    WHERE BookingID = @BookingID AND Status <> 'Cancelled' AND Status <> 'Confirmed';
    
    -- Return number of rows affected
    SELECT @@ROWCOUNT AS RowsAffected;
END







--Flight Management

CREATE PROCEDURE UpdateFlight
    @FlightID INT,
    @AirlineID INT,
    @FlightNumber VARCHAR(20),
    @Origin VARCHAR(100),
    @Destination VARCHAR(100),
    @DepartureTime DATETIME,
    @ArrivalTime DATETIME,
    @Duration VARCHAR(20),
    @Price DECIMAL(10,2),
    @Status VARCHAR(20)
AS
BEGIN
    UPDATE Flights
    SET AirlineID = @AirlineID,
        FlightNumber = @FlightNumber,
        Origin = @Origin,
        Destination = @Destination,
        DepartureTime = @DepartureTime,
        ArrivalTime = @ArrivalTime,
        Duration = @Duration,
        Price = @Price,
        Status = @Status
    WHERE FlightID = @FlightID
END
GO

CREATE PROCEDURE CancelFlight
    @FlightID INT
AS
BEGIN
    UPDATE Flights
    SET Status = 'Cancelled'
    WHERE FlightID = @FlightID;
END


CREATE PROCEDURE ShowAllFlights
AS
BEGIN
    SELECT
        f.FlightID,
        a.Name AS AirlineName,
        f.FlightNumber,
        f.Origin,
        f.Destination,
        f.DepartureTime,
        f.ArrivalTime,
        f.Duration,
        f.Price,
        f.Status
    FROM Flights f
    JOIN Airlines a ON f.AirlineID = a.AirlineID
    ORDER BY f.DepartureTime ASC;
END




-- Add Flight
CREATE PROCEDURE AddFlight
    @AirlineID INT,
    @FlightNumber VARCHAR(20),
    @Origin VARCHAR(100),
    @Destination VARCHAR(100),
    @DepartureTime DATETIME,
    @ArrivalTime DATETIME,
    @Duration VARCHAR(20),
    @Price DECIMAL(10,2),
    @Status VARCHAR(20)
AS
BEGIN
    INSERT INTO Flights (AirlineID, FlightNumber, Origin, Destination, DepartureTime, ArrivalTime, Duration, Price, Status)
    VALUES (@AirlineID, @FlightNumber, @Origin, @Destination, @DepartureTime, @ArrivalTime, @Duration, @Price, @Status)
END
GO




--User management
CREATE PROCEDURE sp_ViewAllUsers
AS
BEGIN
    SELECT 
        UserID, 
        FullName, 
        CNIC, 
        Email, 
        Phone, 
        Role 
    FROM dbo.Users
END


CREATE PROCEDURE AddUsers
    @FullName NVARCHAR(100),
    @CNIC VARCHAR(15),
    @Email NVARCHAR(100),
    @Password NVARCHAR(100),
    @Phone VARCHAR(15),
    @Role NVARCHAR(50)
AS
BEGIN
    INSERT INTO Users (FullName, CNIC, Email, Password, Phone, Role)
    VALUES (@FullName, @CNIC, @Email, @Password, @Phone, @Role);
END



CREATE PROCEDURE sp_UpdateUser
    @UserID INT,
    @FullName NVARCHAR(100),
    @Email NVARCHAR(100),
    @Phone NVARCHAR(20),
    @Role NVARCHAR(10)
AS
BEGIN
    UPDATE dbo.Users
    SET 
        FullName = @FullName, 
        Email = @Email, 
        Phone = @Phone, 
        Role = @Role
    WHERE UserID = @UserID
END


CREATE PROCEDURE sp_DeleteUser
    @UserID INT
AS
BEGIN
    DELETE FROM dbo.Users WHERE UserID = @UserID
END

select * from Bookings
select * from bookedseats
select * from users
select * from seats



Use AirlineReservationSystem
go

CREATE PROCEDURE ShowAllFlights
AS
BEGIN
    SELECT 
	A.Name AS AirlineName,
        F.FlightNumber,
        F.Origin,
        F.Destination,
        F.DepartureTime,
        F.ArrivalTime,
        F.Price
    FROM Flights F
    INNER JOIN Airlines A ON F.AirlineID = A.AirlineID
    WHERE F.Status = 'Scheduled'; 
END
GO

ALTER PROCEDURE ShowAllFlights
AS
BEGIN
    SELECT 
        F.FlightID,              -- ✅ Add this line
        A.Name AS AirlineName,
        F.FlightNumber,
        F.Origin,
        F.Destination,
        F.DepartureTime,
        F.ArrivalTime,
        F.Price
    FROM Flights F
    INNER JOIN Airlines A ON F.AirlineID = A.AirlineID
    WHERE F.Status = 'Scheduled'; 
END
GO


Use AirlineReservationSystem

ALTER PROCEDURE UpdateUserProfile
    @UserID INT,
    @FullName VARCHAR(100),
    @Email VARCHAR(100),
    @Password VARCHAR(100),
    @Phone VARCHAR(20),
    @CNIC VARCHAR(20),
    @Role VARCHAR(20)
AS
BEGIN
    UPDATE Users
    SET FullName = @FullName,
        Email = @Email,
        Password = @Password,
        Phone = @Phone,
        CNIC = @CNIC,
        Role = @Role
    WHERE UserID = @UserID;
END



select * from users



USE AirlineReservationSystem;
GO

CREATE PROCEDURE GetUserBookings
    @UserID INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        b.BookingID,
        f.FlightID,
        a.Name AS AirlineName,
        f.FlightNumber,
        f.Origin,
        f.Destination,
        f.DepartureTime,
        f.ArrivalTime,
        f.Price,
        s.SeatNumber,
        s.Class AS SeatClass,
        b.BookingDate,
        b.TotalAmount,
        b.Status
    FROM Bookings b
    JOIN Flights f ON b.FlightID = f.FlightID
    JOIN Airlines a ON f.AirlineID = a.AirlineID
    LEFT JOIN BookedSeats bs ON b.BookingID = bs.BookingID
    LEFT JOIN Seats s ON bs.SeatID = s.SeatID
    WHERE b.UserID = @UserID
    ORDER BY f.DepartureTime;
END;
GO



CREATE PROCEDURE UpdateUserProfile
    @UserID INT,
    @FullName VARCHAR(100),
    @Email VARCHAR(100),
    @Phone VARCHAR(20),
    @Status INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Users
    SET FullName = @FullName,
        Email = @Email,
        Phone = @Phone
    WHERE UserID = @UserID;

    IF @@ROWCOUNT > 0
        SET @Status = 1;  -- Update successful
    ELSE
        SET @Status = 0;  -- No rows updated / user not found
END


select * from users