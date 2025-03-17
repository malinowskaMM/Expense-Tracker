import { createTheme, ThemeProvider } from '@mui/material/styles';
import { plPL, enUS } from '@mui/material/locale';
import { useTranslation } from 'react-i18next';

const AppThemeProvider = ({ children }) => {
  const { i18n } = useTranslation();

  const theme = createTheme({
    // Twój temat
  }, i18n.language === 'plPL' ? plPL : enUS);

  return (
    <ThemeProvider theme={theme}>
      {children}
    </ThemeProvider>
  );
};

export default AppThemeProvider;
