/*
 * Copyright (c) 2002-2013, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.appointment.web;

import fr.paris.lutece.plugins.appointment.business.Appointment;
import fr.paris.lutece.plugins.appointment.business.AppointmentDTO;
import fr.paris.lutece.plugins.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.appointment.business.AppointmentFormHome;
import fr.paris.lutece.plugins.appointment.business.AppointmentFormMessages;
import fr.paris.lutece.plugins.appointment.business.AppointmentFormMessagesHome;
import fr.paris.lutece.plugins.appointment.business.AppointmentHome;
import fr.paris.lutece.plugins.appointment.business.calendar.AppointmentDay;
import fr.paris.lutece.plugins.appointment.business.calendar.AppointmentDayHome;
import fr.paris.lutece.plugins.appointment.business.calendar.AppointmentSlot;
import fr.paris.lutece.plugins.appointment.business.calendar.AppointmentSlotHome;
import fr.paris.lutece.plugins.appointment.service.AppointmentFormService;
import fr.paris.lutece.plugins.appointment.service.CalendarService;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryFilter;
import fr.paris.lutece.plugins.genericattributes.business.EntryHome;
import fr.paris.lutece.plugins.genericattributes.business.GenericAttributeError;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.business.ResponseHome;
import fr.paris.lutece.portal.service.captcha.CaptchaSecurityService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.util.beanvalidation.BeanValidationUtil;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;

import org.apache.commons.lang.StringUtils;


/**
 * This class provides a simple implementation of an XPage
 */
@Controller( xpageName = "appointment", pageTitleI18nKey = "appointment.appointmentApp.defaultTitle", pagePathI18nKey = "appointment.appointmentApp.defaultPath" )
public class AppointmentApp extends MVCApplication
{
    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 5741361182728887387L;

    // Templates
    private static final String TEMPLATE_APPOINTMENT_FORM_LIST = "/skin/plugins/appointment/appointment_form_list.html";
    private static final String TEMPLATE_APPOINTMENT_FORM = "/skin/plugins/appointment/appointment_form.html";
    private static final String TEMPLATE_APPOINTMENT_FORM_CALENDAR = "/skin/plugins/appointment/appointment_form_calendar.html";
    private static final String TEMPLATE_APPOINTMENT_FORM_RECAP = "/skin/plugins/appointment/appointment_form_recap.html";
    private static final String TEMPLATE_APPOINTMENT_CREATED = "skin/plugins/appointment/appointment_created.html";

    // Views
    private static final String VIEW_APPOINTMENT_FORM_LIST = "getViewFormList";
    private static final String VIEW_GET_FORM = "viewForm";
    private static final String VIEW_GET_APPOINTMENT_CALENDAR = "getAppointmentCalendar";
    private static final String VIEW_DISPLAY_RECAP_APPOINTMENT = "displayRecapAppointment";
    private static final String VIEW_GET_APPOINTMENT_CREATED = "getAppointmentCreated";

    // Actions
    private static final String ACTION_DO_VALIDATE_FORM = "doValidateForm";
    private static final String ACTION_DO_MAKE_APPOINTMENT = "doMakeAppointment";

    // Parameters
    private static final String PARAMETER_ID_FORM = "id_form";
    private static final String PARAMETER_NB_WEEK = "nb_week";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_FIRST_NAME = "firstname";
    private static final String PARAMETER_LAST_NAME = "lastname";
    private static final String PARAMETER_ID_SLOT = "idSlot";

    //    private static final String PARAMETER_DATE = "date";
    private static final String PARAMETER_BACK = "back";

    // Marks
    private static final String MARK_FORM_LIST = "form_list";
    private static final String MARK_FORM_HTML = "form_html";
    private static final String MARK_FORM_ERRORS = "form_errors";
    private static final String MARK_LIST_DAYS = "listDays";
    private static final String MARK_FORM = "form";
    private static final String MARK_DAY = "day";
    private static final String MARK_FORM_MESSAGES = "formMessages";
    private static final String MARK_LIST_TIME_BEGIN = "list_time_begin";
    private static final String MARK_MIN_DURATION_APPOINTMENT = "min_duration_appointments";
    private static final String MARK_APPOINTMENT = "appointment";
    private static final String MARK_CAPTCHA = "captcha";
    private static final String MARK_SLOT = "slot";
    private static final String MARK_LIST_DAYS_OF_WEEK = "list_days_of_week";

    // Errors
    private static final String ERROR_MESSAGE_SLOT_FULL = "appointment.message.error.slotFull";
    private static final String ERROR_MESSAGE_CAPTCHA = "portal.admin.message.wrongCaptcha";

    // Session keys
    private static final String SESSION_APPOINTMENT_FORM_ERRORS = "appointment.session.formErrors";

    // Messages
    private static final String[] MESSAGE_LIST_DAYS_OF_WEEK = CalendarService.getListDaysOfWeek( );

    // Local variables
    private final AppointmentFormService _appointmentFormService = SpringContextService.getBean( AppointmentFormService.BEAN_NAME );
    private transient CaptchaSecurityService _captchaSecurityService;

    //    private transient DateConverter _dateConverter;

    /**
     * Get the list of appointment form list
     * @param request The request
     * @return The XPage to display
     */
    @View( value = VIEW_APPOINTMENT_FORM_LIST, defaultView = true )
    public XPage getFormList( HttpServletRequest request )
    {
        _appointmentFormService.removeAppointmentFromSession( request.getSession(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );

        Collection<AppointmentForm> listAppointmentForm = AppointmentFormHome.getActiveAppointmentFormsList(  );
        model.put( MARK_FORM_LIST, listAppointmentForm );

        return getXPage( TEMPLATE_APPOINTMENT_FORM_LIST, request.getLocale(  ), model );
    }

    /**
     * Get the page to complete a form
     * @param request The request
     * @return The XPage to display
     */
    @View( VIEW_GET_FORM )
    public XPage getViewForm( HttpServletRequest request )
    {
        String strIdForm = request.getParameter( PARAMETER_ID_FORM );

        if ( ( strIdForm != null ) && StringUtils.isNumeric( strIdForm ) )
        {
            int nIdForm = Integer.parseInt( strIdForm );

            AppointmentForm form = AppointmentFormHome.findByPrimaryKey( nIdForm );
            AppointmentFormMessages formMessages = AppointmentFormMessagesHome.findByPrimaryKey( nIdForm );

            if ( ( form == null ) || !form.getIsActive(  ) )
            {
                return redirectView( request, VIEW_APPOINTMENT_FORM_LIST );
            }

            Map<String, Object> model = new HashMap<String, Object>(  );

            Appointment appointment = _appointmentFormService.getValidatedAppointmentFromSession( request.getSession(  ) );

            if ( appointment != null )
            {
                AppointmentDTO appointmentDTO = new AppointmentDTO(  );
                appointmentDTO.setEmail( appointment.getEmail(  ) );
                appointmentDTO.setFirstName( appointment.getEmail(  ) );
                appointmentDTO.setLastName( appointment.getLastName(  ) );

                Map<Integer, List<Response>> mapResponsesByIdEntry = appointmentDTO.getMapResponsesByIdEntry(  );

                for ( Response response : appointment.getListResponse(  ) )
                {
                    List<Response> listResponse = mapResponsesByIdEntry.get( response.getEntry(  ).getIdEntry(  ) );

                    if ( listResponse == null )
                    {
                        listResponse = new ArrayList<Response>(  );
                        mapResponsesByIdEntry.put( response.getEntry(  ).getIdEntry(  ), listResponse );
                    }

                    listResponse.add( response );
                }

                _appointmentFormService.saveAppointmentInSession( request.getSession(  ), appointmentDTO );
            }

            model.put( MARK_FORM_HTML,
                _appointmentFormService.getHtmlForm( form, formMessages, getLocale( request ), true, request ) );

            List<GenericAttributeError> listErrors = (List<GenericAttributeError>) request.getSession(  )
                                                                                          .getAttribute( SESSION_APPOINTMENT_FORM_ERRORS );

            if ( listErrors != null )
            {
                model.put( MARK_FORM_ERRORS, listErrors );
                request.getSession(  ).removeAttribute( SESSION_APPOINTMENT_FORM_ERRORS );
            }

            _appointmentFormService.removeAppointmentFromSession( request.getSession(  ) );
            _appointmentFormService.removeValidatedAppointmentFromSession( request.getSession(  ) );

            XPage page = getXPage( TEMPLATE_APPOINTMENT_FORM, getLocale( request ), model );

            if ( form.getDisplayTitleFo(  ) )
            {
                page.setTitle( form.getTitle(  ) );
            }

            return page;
        }

        return redirectView( request, VIEW_APPOINTMENT_FORM_LIST );
    }

    /**
     * Do validate data entered by a user to fill a form
     * @param request The request
     * @return The next URL to redirect to
     */
    @Action( ACTION_DO_VALIDATE_FORM )
    public XPage doValidateForm( HttpServletRequest request )
    {
        String strIdForm = request.getParameter( PARAMETER_ID_FORM );

        if ( ( strIdForm != null ) && StringUtils.isNumeric( strIdForm ) )
        {
            int nIdForm = Integer.parseInt( strIdForm );

            EntryFilter filter = new EntryFilter(  );
            filter.setIdResource( nIdForm );
            filter.setResourceType( AppointmentForm.RESOURCE_TYPE );
            filter.setEntryParentNull( EntryFilter.FILTER_TRUE );
            filter.setFieldDependNull( EntryFilter.FILTER_TRUE );
            filter.setIdIsComment( EntryFilter.FILTER_FALSE );

            List<Entry> listEntryFirstLevel = EntryHome.getEntryList( filter );

            _appointmentFormService.removeAppointmentFromSession( request.getSession(  ) );

            List<GenericAttributeError> listFormErrors = new ArrayList<GenericAttributeError>(  );
            Locale locale = request.getLocale(  );

            AppointmentDTO appointment = new AppointmentDTO(  );
            appointment.setEmail( request.getParameter( PARAMETER_EMAIL ) );
            appointment.setFirstName( request.getParameter( PARAMETER_FIRST_NAME ) );
            appointment.setLastName( request.getParameter( PARAMETER_LAST_NAME ) );
            appointment.setStatus( Appointment.STATUS_NOT_VALIDATED );

            if ( SecurityService.isAuthenticationEnable(  ) )
            {
                LuteceUser luteceUser = SecurityService.getInstance(  ).getRegisteredUser( request );

                if ( luteceUser != null )
                {
                    appointment.setIdUser( luteceUser.getName(  ) );
                }
            }

            // We save the appointment in session. The appointment object will contain responses of the user to the form
            _appointmentFormService.saveAppointmentInSession( request.getSession(  ), appointment );

            Set<ConstraintViolation<AppointmentDTO>> listErrors = BeanValidationUtil.validate( appointment );

            if ( !listErrors.isEmpty(  ) )
            {
                for ( ConstraintViolation<AppointmentDTO> constraintViolation : listErrors )
                {
                    GenericAttributeError genAttError = new GenericAttributeError(  );
                    genAttError.setErrorMessage( constraintViolation.getMessage(  ) );
                    listFormErrors.add( genAttError );
                }
            }

            for ( Entry entry : listEntryFirstLevel )
            {
                listFormErrors.addAll( _appointmentFormService.getResponseEntry( request, entry.getIdEntry(  ), locale,
                        appointment ) );
            }

            // If there is some errors, we redirect the user to the form page
            if ( listFormErrors.size(  ) > 0 )
            {
                request.getSession(  ).setAttribute( SESSION_APPOINTMENT_FORM_ERRORS, listFormErrors );

                return redirect( request, VIEW_GET_FORM, PARAMETER_ID_FORM, nIdForm );
            }

            _appointmentFormService.convertMapResponseToList( appointment );
            _appointmentFormService.saveValidatedAppointmentForm( request.getSession(  ), appointment );

            return redirect( request, VIEW_GET_APPOINTMENT_CALENDAR, PARAMETER_ID_FORM, nIdForm );
        }

        return redirectView( request, VIEW_APPOINTMENT_FORM_LIST );
    }

    /**
     * Get the page with the calendar with opened and closed days for an
     * appointment form
     * @param request The request
     * @return The XPage to display
     */
    @View( VIEW_GET_APPOINTMENT_CALENDAR )
    public XPage getAppointmentCalendar( HttpServletRequest request )
    {
        String strIdForm = request.getParameter( PARAMETER_ID_FORM );

        if ( ( strIdForm != null ) && StringUtils.isNumeric( strIdForm ) )
        {
            int nIdForm = Integer.parseInt( strIdForm );

            if ( _appointmentFormService.getValidatedAppointmentFromSession( request.getSession(  ) ) == null )
            {
                return redirect( request, VIEW_GET_FORM, PARAMETER_ID_FORM, nIdForm );
            }

            AppointmentForm form = AppointmentFormHome.findByPrimaryKey( nIdForm );
            AppointmentFormMessages formMessages = AppointmentFormMessagesHome.findByPrimaryKey( nIdForm );

            Map<String, Object> model = new HashMap<String, Object>(  );

            String strNbWeek = request.getParameter( PARAMETER_NB_WEEK );
            int nNbWeek = 0;

            if ( StringUtils.isNotEmpty( strNbWeek ) && StringUtils.isNumeric( strNbWeek ) )
            {
                nNbWeek = Integer.parseInt( strNbWeek );

                if ( nNbWeek > ( form.getNbWeeksToDisplay(  ) - 1 ) )
                {
                    nNbWeek = form.getNbWeeksToDisplay(  ) - 1;
                }
            }

            List<AppointmentDay> listDays = CalendarService.getService(  ).getDayListForCalendar( form, nNbWeek );

            List<String> listTimeBegin = new ArrayList<String>(  );
            int nMinAppointmentDuration = CalendarService.getService(  ).getListTimeBegin( listDays, form, listTimeBegin );

            model.put( MARK_FORM, form );
            model.put( MARK_FORM_MESSAGES, formMessages );
            model.put( MARK_LIST_DAYS, listDays );
            model.put( MARK_LIST_TIME_BEGIN, listTimeBegin );
            model.put( MARK_MIN_DURATION_APPOINTMENT, nMinAppointmentDuration );
            model.put( PARAMETER_NB_WEEK, nNbWeek );
            model.put( MARK_LIST_DAYS_OF_WEEK, MESSAGE_LIST_DAYS_OF_WEEK );

            return getXPage( TEMPLATE_APPOINTMENT_FORM_CALENDAR, getLocale( request ), model );
        }

        return redirectView( request, VIEW_APPOINTMENT_FORM_LIST );
    }

    /**
     * Display the recap before validating an appointment
     * @param request The request
     * @return The HTML content to display or the next URL to redirect to
     */
    @View( VIEW_DISPLAY_RECAP_APPOINTMENT )
    public XPage displayRecapAppointment( HttpServletRequest request )
    {
        String strIdSlot = request.getParameter( PARAMETER_ID_SLOT );

        if ( StringUtils.isNotEmpty( strIdSlot ) && StringUtils.isNumeric( strIdSlot ) )
        {
            Appointment appointment = _appointmentFormService.getValidatedAppointmentFromSession( request.getSession(  ) );

            int nIdSlot = Integer.parseInt( strIdSlot );
            AppointmentSlot appointmentSlot = AppointmentSlotHome.findByPrimaryKey( nIdSlot );

            if ( appointment != null )
            {
                appointment.setIdSlot( nIdSlot );

                Map<String, Object> model = new HashMap<String, Object>(  );
                model.put( MARK_APPOINTMENT, appointment );
                model.put( MARK_SLOT, appointmentSlot );

                AppointmentForm form = AppointmentFormHome.findByPrimaryKey( appointmentSlot.getIdForm(  ) );

                if ( form.getEnableCaptcha(  ) && getCaptchaService(  ).isAvailable(  ) )
                {
                    model.put( MARK_CAPTCHA, getCaptchaService(  ).getHtmlCode(  ) );
                }

                AppointmentDay day = AppointmentDayHome.findByPrimaryKey( appointmentSlot.getIdDay(  ) );
                appointment.setDateAppointment( (Date) day.getDate(  ).clone(  ) );
                model.put( MARK_FORM_MESSAGES, AppointmentFormMessagesHome.findByPrimaryKey( form.getIdForm(  ) ) );
                model.put( MARK_DAY, day );
                model.put( MARK_FORM, form );
                fillCommons( model );

                return getXPage( TEMPLATE_APPOINTMENT_FORM_RECAP, getLocale( request ), model );
            }

            return redirect( request, VIEW_GET_APPOINTMENT_CALENDAR, PARAMETER_ID_FORM, appointmentSlot.getIdForm(  ) );
        }

        return redirectView( request, VIEW_GET_FORM );
    }

    /**
     * Do save an appointment into the database if it is valid
     * @param request The request
     * @return The XPage to display
     */
    @Action( ACTION_DO_MAKE_APPOINTMENT )
    public XPage doMakeAppointment( HttpServletRequest request )
    {
        Appointment appointment = _appointmentFormService.getValidatedAppointmentFromSession( request.getSession(  ) );
        AppointmentSlot appointmentSlot = AppointmentSlotHome.findByPrimaryKeyWithFreePlaces( appointment.getIdSlot(  ),
                appointment.getDateAppointment(  ) );
        AppointmentForm form = AppointmentFormHome.findByPrimaryKey( appointmentSlot.getIdForm(  ) );

        if ( form.getEnableCaptcha(  ) && getCaptchaService(  ).isAvailable(  ) )
        {
            if ( !getCaptchaService(  ).validate( request ) )
            {
                addError( ERROR_MESSAGE_CAPTCHA, getLocale( request ) );

                return redirect( request, VIEW_DISPLAY_RECAP_APPOINTMENT, PARAMETER_ID_SLOT,
                    appointmentSlot.getIdSlot(  ) );
            }
        }

        if ( StringUtils.isNotEmpty( request.getParameter( PARAMETER_BACK ) ) )
        {
            return redirect( request, VIEW_GET_APPOINTMENT_CALENDAR, PARAMETER_ID_FORM, appointmentSlot.getIdForm(  ) );
        }

        if ( appointmentSlot.getNbFreePlaces(  ) <= 0 )
        {
            addError( ERROR_MESSAGE_SLOT_FULL, getLocale( request ) );

            return redirect( request, VIEW_GET_APPOINTMENT_CALENDAR, PARAMETER_ID_FORM, appointmentSlot.getIdForm(  ) );
        }

        AppointmentHome.create( appointment );

        for ( Response response : appointment.getListResponse(  ) )
        {
            ResponseHome.create( response );
            AppointmentHome.insertAppointmentResponse( appointment.getIdAppointment(  ), response.getIdResponse(  ) );
        }

        if ( form.getIdWorkflow(  ) > 0 )
        {
            WorkflowService.getInstance(  )
                           .getState( appointment.getIdAppointment(  ), Appointment.APPOINTMENT_RESOURCE_TYPE,
                form.getIdWorkflow(  ), form.getIdForm(  ) );
            WorkflowService.getInstance(  )
                           .executeActionAutomatic( appointment.getIdAppointment(  ),
                Appointment.APPOINTMENT_RESOURCE_TYPE, form.getIdWorkflow(  ), form.getIdForm(  ) );
        }

        _appointmentFormService.removeValidatedAppointmentFromSession( request.getSession(  ) );

        return redirect( request, VIEW_GET_APPOINTMENT_CREATED, PARAMETER_ID_FORM, appointmentSlot.getIdForm(  ) );
    }

    /**
     * Get the page to notify the user that the appointment has been created
     * @param request The request
     * @return The XPage to display
     */
    @View( VIEW_GET_APPOINTMENT_CREATED )
    public XPage getAppointmentCreated( HttpServletRequest request )
    {
        String strIdForm = request.getParameter( PARAMETER_ID_FORM );

        if ( StringUtils.isNotEmpty( strIdForm ) && StringUtils.isNumeric( strIdForm ) )
        {
            int nIdForm = Integer.parseInt( strIdForm );
            AppointmentForm form = AppointmentFormHome.findByPrimaryKey( nIdForm );
            AppointmentFormMessages formMessages = AppointmentFormMessagesHome.findByPrimaryKey( nIdForm );
            Map<String, Object> model = new HashMap<String, Object>(  );
            model.put( MARK_FORM, form );
            model.put( MARK_FORM_MESSAGES, formMessages );

            return getXPage( TEMPLATE_APPOINTMENT_CREATED, getLocale( request ), model );
        }

        return redirectView( request, VIEW_APPOINTMENT_FORM_LIST );
    }

    //    /**
    //     * Get the converter to convert string to java.sql.Date.
    //     * @return The converter to convert String to java.sql.Date.
    //     */
    //    private DateConverter getDateConverter( )
    //    {
    //        if ( _dateConverter == null )
    //        {
    //            _dateConverter = new DateConverter( DateFormat.getDateInstance( DateFormat.SHORT, Locale.FRANCE ) );
    //        }
    //
    //        return _dateConverter;
    //    }

    /**
     * Get the captcha security service
     * @return The captcha security service
     */
    private CaptchaSecurityService getCaptchaService(  )
    {
        if ( _captchaSecurityService == null )
        {
            _captchaSecurityService = new CaptchaSecurityService(  );
        }

        return _captchaSecurityService;
    }
}