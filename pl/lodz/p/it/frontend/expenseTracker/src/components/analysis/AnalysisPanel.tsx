import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChartBar } from '@fortawesome/free-solid-svg-icons';
import { useTranslation } from "react-i18next";
import CustomTabPanel from './CustomTabPanel';

const AnalysisPanel = () => {
    const { t, i18n } = useTranslation();

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

    return (
        <div>
            <div className="font-link" style={headerTextStyle}>
                <span>
                    <FontAwesomeIcon icon={faChartBar} style={iconStyle} />
                    {t('analysisPanel.analysis')}
                </span>
        </div>
        <CustomTabPanel/>
        </div>
    )
}

export default AnalysisPanel;