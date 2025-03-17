import * as React from 'react';
import { Box, Typography, Tab, Tabs } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { styled } from "@mui/material/styles";
import ExpensesAndIncomesAnalysisPanel from './ExpensesAndIncomesAnalysisPanel';
import ExpensesPerCategoryAnalysisPanel from './ExpensesPerCategoryAnalysisPanel';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function CustomTabPanel(props: TabPanelProps) {
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
        <Tabs value={value} onChange={handleChange} aria-label="basic tabs example" style={{ fontFamily: "Inter", fontSize: '1.5rem'}}
        sx={{
            ".Mui-selected": {
            color: `rgba(131, 225, 180, 1)`
            },
            }}>
          <Tab label={t('analysisPanel.expensesAndIncomes')} {...a11yProps(0)} />
          <Tab label={t('analysisPanel.expensesPerCategory')} {...a11yProps(1)} />
        </Tabs>
      </Box>
      <CustomTabPanel value={value} index={0}>
            <ExpensesAndIncomesAnalysisPanel />
      </CustomTabPanel>
      <CustomTabPanel value={value} index={1}>
            <ExpensesPerCategoryAnalysisPanel />
      </CustomTabPanel>
    </Box>
  );
}
