import * as React from 'react';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useTranslation } from "react-i18next";
import "./CalendarTabPanel.css"
import CalendarIncomeTabPanel from './CalendarIncomeTabPanel';
import CalendarExpenseTabPanel from './CalendarExpenseTabPanel';

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function CalendarTabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{ p: 3 }}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
}

function a11yProps(index: number) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

export default function BasicTabs() {
    const [value, setValue] = React.useState(0);
    const { t, i18n } = useTranslation();

    const handleChange = (event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
    };

    return (
        <Box sx={{ width: '100%' }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
                    <Tab label={t('calendarPanel.income')} {...a11yProps(0)} />
                    <Tab label={t('calendarPanel.expense')} {...a11yProps(1)} />
                </Tabs>
            </Box>
            <CalendarTabPanel value={value} index={0}>
                <CalendarIncomeTabPanel />
            </CalendarTabPanel>
            <CalendarTabPanel value={value} index={1}>
                <CalendarExpenseTabPanel />
            </CalendarTabPanel>
        </Box>
    );
}
