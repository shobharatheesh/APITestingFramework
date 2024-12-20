package com.testingacademy.tests.integration;


import com.testingacademy.base.BaseTest;
import com.testingacademy.endpoints.APIConstants;
import com.testingacademy.pojos.BookingRespons;
import io.qameta.allure.Description;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

public class TestIntegrationFlow2 extends BaseTest {

// Create Booking -> Delete it -> Verify
//
//Get a Booking from Get All -> Try to Delete that booking
//
//Create Booking -> Update it -> Try to Delete
//
//Delete a Booking -> Try to Update it

    @Test(priority = 1)
    @Owner("Shobha")
    @Description("Test 1: Create a booking, delete it, and verify deletion")
    public void testCreateAndDeleteBooking(ITestContext iTestContext) {
        // Create a booking
        requestSpecification.basePath(APIConstants.CREATE_UPDATE_BOOKING_URL);
        response = RestAssured.given(requestSpecification)
                .body(payloadManager.createPayloadBookingAsString()).post();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(200);

        BookingRespons bookingResponse = payloadManager.bookingResponseJava(response.asString());
        Integer bookingId = bookingResponse.getBookingid();
        iTestContext.setAttribute("bookingid", bookingId);

        // Delete the booking with token
        String token = getToken(); // Get token
        String deletePath = APIConstants.CREATE_UPDATE_BOOKING_URL + "/" + bookingId;
        requestSpecification.basePath(deletePath).cookie("token", token);
        response = RestAssured.given(requestSpecification).delete();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(201);

        // Verify the booking no longer exists
        response = RestAssured.given(requestSpecification).get();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(404);
    }

    @Test(priority = 2)
    @Owner("Rohit")
    @Description("Test 2: Get a booking from all bookings and try to delete it")
    public void testDeleteExistingBooking() {
        // Get a booking from all bookings
        requestSpecification.basePath(APIConstants.CREATE_UPDATE_BOOKING_URL);
        response = RestAssured.given(requestSpecification).get();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(200);

        // Parse first booking ID
        Integer bookingId = response.jsonPath().getInt("[0].bookingid");

        // Delete the booking with token
        String token = getToken(); // Get token
        String deletePath = APIConstants.CREATE_UPDATE_BOOKING_URL + "/" + bookingId;
        requestSpecification.basePath(deletePath).cookie("token", token);
        response = RestAssured.given(requestSpecification).delete();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(201);
    }


    @Test(priority = 3)
    @Owner("Rohit")
    @Description("Test 3: Create a booking, update it, and delete it")
    public void testCreateUpdateDeleteBooking(ITestContext iTestContext) {
        // Create a booking
        requestSpecification.basePath(APIConstants.CREATE_UPDATE_BOOKING_URL);
        response = RestAssured.given(requestSpecification)
                .body(payloadManager.createPayloadBookingAsString()).post();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(200);

        BookingRespons bookingResponse = payloadManager.bookingResponseJava(response.asString());
        Integer bookingId = bookingResponse.getBookingid();
        iTestContext.setAttribute("bookingid", bookingId);

        // Update the booking
        String updatePath = APIConstants.CREATE_UPDATE_BOOKING_URL + "/" + bookingId;
        String token = getToken();
        requestSpecification.basePath(updatePath);
        response = RestAssured.given(requestSpecification).cookie("token", token)
                .body(payloadManager.fullUpdatePayloadAsString()).put();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(200);

        // Delete the booking
        String deletePath = APIConstants.CREATE_UPDATE_BOOKING_URL + "/" + bookingId;
        requestSpecification.basePath(deletePath);
        response = RestAssured.given(requestSpecification).cookie("token", token).delete();
        validatableResponse = response.then().log().all();
        validatableResponse.statusCode(201);
    }

    @Test(groups = "qa", priority = 4)
    @Owner("Rohit")
    @Description("Test 4: Delete a booking and verify if it is deleted correctly")
    public void testDeleteBooking(ITestContext iTestContext) {
        // Retrieve bookingId and token from previous tests
        Integer bookingId = (Integer) iTestContext.getAttribute("bookingid");
        String token = (String) iTestContext.getAttribute("token");

        // Ensure bookingId and token are available
        if (bookingId == null || token == null) {
            Assert.fail("Booking ID or Token is null. Cannot proceed with deletion.");
        }

        // Print the bookingId and token for debugging
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Token: " + token);

        // Check if the booking exists before attempting deletion
        String getBookingPath = APIConstants.CREATE_UPDATE_BOOKING_URL + "/" + bookingId;
        requestSpecification.basePath(getBookingPath);
        response = RestAssured.given(requestSpecification).get();

        // Debugging response status
        System.out.println("Get booking response status: " + response.getStatusCode());
        System.out.println("Get booking response body: " + response.asString());

        // If booking exists, proceed to delete
        if (response.getStatusCode() == 200) {
            // Proceed with DELETE request
            String deletePath = APIConstants.CREATE_UPDATE_BOOKING_URL + "/" + bookingId;
            requestSpecification.basePath(deletePath).cookie("token", token);

            response = RestAssured.given(requestSpecification).delete();

            // Log response for debugging
            System.out.println("Delete booking response status: " + response.getStatusCode());
            System.out.println("Delete booking response body: " + response.asString());

            // Assert that the response status code is 201 (Created) after successful deletion
            validatableResponse = response.then().log().all();
            validatableResponse.statusCode(201); // Deletion should return 201 (for successful action)

            // Verify the booking has been deleted by trying to retrieve it again
            response = RestAssured.given(requestSpecification).get();

            // Assert that the booking is no longer available (404 Not Found)
            validatableResponse = response.then().log().all();
            validatableResponse.statusCode(404); // After deletion, the booking should no longer exist
        } else {
            // If the booking doesn't exist, fail the test with a descriptive message
            System.out.println("Booking with ID " + bookingId + " does not exist.");
            Assert.fail("Booking with ID " + bookingId + " does not exist. Deletion cannot proceed.");
        }
    }

}
