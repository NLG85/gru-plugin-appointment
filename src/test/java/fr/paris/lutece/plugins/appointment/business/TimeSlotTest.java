package fr.paris.lutece.plugins.appointment.business;

import java.time.LocalTime;

import fr.paris.lutece.plugins.appointment.business.planningdefinition.TimeSlot;
import fr.paris.lutece.plugins.appointment.business.planningdefinition.TimeSlotHome;
import fr.paris.lutece.plugins.appointment.business.planningdefinition.WorkingDay;
import fr.paris.lutece.plugins.appointment.business.planningdefinition.WorkingDayHome;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * Test class for the TimeSlot
 * 
 * @author Laurent Payen
 *
 */
public class TimeSlotTest extends LuteceTestCase {

	private final static LocalTime STARTING_HOUR_1 = LocalTime.parse("09:00");
	private final static LocalTime STARTING_HOUR_2 = LocalTime.parse("09:30");
	private final static LocalTime ENDING_HOUR_1 = LocalTime.parse("09:30");
	private final static LocalTime ENDING_HOUR_2 = LocalTime.parse("10:00");
	private final static boolean IS_OPEN_1 = true;
	private final static boolean IS_OPEN_2 = false;

	/**
	 * Test method for the TimeSlot (CRUD)
	 */
	public void testTimeSlot() {
		// Initialize a TimeSlot
		TimeSlot timeSlot = buildTimeSlot();
		// Create the TimeSlot in database
		TimeSlotHome.create(timeSlot);
		// Find the TimeSlot created in database
		TimeSlot timeSlotStored = TimeSlotHome.findByPrimaryKey(timeSlot.getIdTimeSlot());
		// Check Asserts
		checkAsserts(timeSlotStored, timeSlot);

		// Update the timeSlot
		timeSlot.setStartingHour(STARTING_HOUR_2);
		timeSlot.setEndingHour(ENDING_HOUR_2);
		timeSlot.setIsOpen(IS_OPEN_2);
		// Update the timeSlot in database
		TimeSlotHome.update(timeSlot);
		// Find the timeSlot updated in database
		timeSlotStored = TimeSlotHome.findByPrimaryKey(timeSlot.getIdTimeSlot());
		// Check Asserts
		checkAsserts(timeSlotStored, timeSlot);

		// Delete the timeSlot
		TimeSlotHome.delete(timeSlot.getIdTimeSlot());
		timeSlotStored = TimeSlotHome.findByPrimaryKey(timeSlot.getIdTimeSlot());
		// Check the timeSlot has been removed from database
		assertNull(timeSlotStored);
	}

	/**
	 * build a TimeSlot Business Object
	 * 
	 * @return the timeSlot
	 */
	public static TimeSlot buildTimeSlot() {
		TimeSlot timeSlot = new TimeSlot();

		timeSlot.setStartingHour(STARTING_HOUR_1);
		timeSlot.setEndingHour(ENDING_HOUR_1);
		timeSlot.setIsOpen(IS_OPEN_1);

		WorkingDay workingDay = WorkingDayTest.buildWorkingDay();
		WorkingDayHome.create(workingDay);
		timeSlot.setIdWorkingDay(workingDay.getIdWorkingDay());

		return timeSlot;
	}

	/**
	 * Check that all the asserts are true
	 * 
	 * @param timeSlotStored
	 *            the timeSlot stored
	 * @param timeSlot
	 *            the timeSlot created
	 */
	public void checkAsserts(TimeSlot timeSlotStored, TimeSlot timeSlot) {
		assertEquals(timeSlotStored.getStartingHour(), timeSlot.getStartingHour());
		assertEquals(timeSlotStored.getEndingHour(), timeSlot.getEndingHour());
		assertEquals(timeSlotStored.isOpen(), timeSlot.isOpen());
		assertEquals(timeSlotStored.getIdWorkingDay(), timeSlot.getIdWorkingDay());
	}

}
