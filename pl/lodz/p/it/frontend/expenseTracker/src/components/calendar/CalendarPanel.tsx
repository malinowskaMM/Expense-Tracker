import dayjs from 'dayjs';
import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCalendarDays } from '@fortawesome/free-solid-svg-icons';
import { StaticDatePicker } from '@mui/x-date-pickers/StaticDatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import Fab from '@mui/material/Fab';
import AddIcon from '@mui/icons-material/Add';
import * as React from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import Slide from '@mui/material/Slide';
import { TransitionProps } from '@mui/material/transitions';
import CalendarTabPanel from './CalendarTabPanel';
import CustomPaginationCalendarTable from '../calendar/CustomPaginationCalendarTable';
import { useState } from 'react';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
      children: React.ReactElement<any, any>;
    },
    ref: React.Ref<unknown>,
  ) {
    return <Slide direction="up" ref={ref} {...props} />;
  });

const CalendarPanel = () => {
    const { t, i18n } = useTranslation();
    const [open, setOpen] = React.useState(false);
    const [dateCalendarValue, setDateCalendarValue] = React.useState(dayjs());
    const [formattedDate, setFormattedDate] = React.useState(dayjs().format('YYYY-MM-DD'));
    const [reloadKey, setReloadKey] = useState(0);
    const [reloadComponent, setReloadComponent] = useState(false);

    const handleClickOpen = () => {
      setOpen(true);
    };
  
    const handleClose = () => {
      setReloadComponent(true);
      setOpen(false);
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '4.0rem',
        color: 'rgba(43, 42, 42, 0.7)',
        paddingTop: '5vh',
    };

    const iconStyle = {
        color: '#99EDC5',
        fontSize: 100,
        paddingRight: '1vw',
    };

    const datePickerStyle = {
        width: '80%',
        aliginItems: 'center',
        justifyContent: 'center',
    };

    const handleDataChange = (newValue: any) => {
        setDateCalendarValue(newValue);
        setFormattedDate(dayjs(newValue).format('YYYY-MM-DD'));
        setReloadKey((prevKey) => prevKey + 1);
      };


    return (
        <div>
            <div className="font-link" style={headerTextStyle}>
                <span>
                    <FontAwesomeIcon icon={faCalendarDays} style={iconStyle} />
                    {t('calendarPanel.calendar')}
                </span>
            </div>
            <div className="container-30-70">
                <div className="left-side-30">
                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                        <div style={datePickerStyle}>
                            <StaticDatePicker 
                            slotProps={{
                                actionBar: {
                                  actions: [],
                                },
                              }}
                            defaultValue={Date.now} 
                            onChange={handleDataChange}
                            
                                                         />
                        </div>
                    </LocalizationProvider>
                </div>
                <div className="right-side-70">
                    <Fab color="primary" aria-label="add" onClick={handleClickOpen}>
                        <AddIcon />
                    </Fab>
                    <CustomPaginationCalendarTable key={reloadKey} dateValue={formattedDate} reloadComponent={reloadComponent}/>
                </div>
            </div>



            <Dialog
                open={open}
                TransitionComponent={Transition}
                keepMounted
                onClose={handleClose}
                aria-describedby="alert-dialog-slide-description"
                PaperProps={{
                    style: {
                        borderRadius: '40px',
                        padding: '2%',
                        backgroundColor: '#bef4da',
                        boxShadow: 'initial'
                    },
                }}
            >
                <DialogTitle>{t('calendarPanel.addElement')}</DialogTitle>
                <CalendarTabPanel/>
            </Dialog>
        </div>
    );
};

export default CalendarPanel;