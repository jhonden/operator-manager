import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  Input,
  Select,
  Button,
  Row,
  Col,
  Tag,
  Space,
  message,
  Empty,
  Spin,
  Rate,
} from 'antd';
import {
  SearchOutlined,
  AppstoreOutlined,
  CodeOutlined,
  StarOutlined,
  DownloadOutlined,
} from '@ant-design/icons';
import type { MarketItem } from '@/types';
import { marketApi } from '@/api/market';
import './market.css';

/**
 * Marketplace list page
 */
const MarketListPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [items, setItems] = useState<MarketItem[]>([]);
  const [featuredItems, setFeaturedItems] = useState<MarketItem[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 12,
    total: 0,
  });

  // Filters
  const [filters, setFilters] = useState({
    keyword: '',
    itemType: undefined as 'OPERATOR' | 'PACKAGE' | undefined,
    category: undefined as number | undefined,
    sortBy: 'createdAt',
    sortOrder: 'desc' as 'asc' | 'desc',
  });

  useEffect(() => {
    fetchFeaturedItems();
    fetchItems();
  }, [pagination.current, pagination.pageSize]);

  const fetchFeaturedItems = async () => {
    try {
      const response = await marketApi.getFeaturedItems();
      if (response.data) {
        setFeaturedItems(response.data);
      }
    } catch (error: any) {
      console.error('Failed to fetch featured items:', error);
    }
  };

  const fetchItems = async () => {
    setLoading(true);
    try {
      const response = await marketApi.searchItems({
        keyword: filters.keyword,
        itemType: filters.itemType,
        categoryId: filters.category,
        sortBy: filters.sortBy,
        sortOrder: filters.sortOrder,
        page: pagination.current - 1,
        size: pagination.pageSize,
      });

      if (response.data) {
        setItems(response.data.content);
        setPagination({
          ...pagination,
          total: response.data.totalElements,
        });
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to fetch market items');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPagination({ ...pagination, current: 1 });
    fetchItems();
  };

  const handleReset = () => {
    setFilters({
      keyword: '',
      itemType: undefined,
      category: undefined,
      sortBy: 'createdAt',
      sortOrder: 'desc',
    });
    setPagination({ ...pagination, current: 1 });
    setTimeout(() => fetchItems(), 0);
  };

  const handleDownload = async (item: MarketItem) => {
    try {
      await marketApi.downloadItem(item.id);
      message.success('Item downloaded successfully');
      // Refresh the item to get updated download count
      fetchItems();
    } catch (error: any) {
      message.error(error.message || 'Failed to download item');
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      {/* Featured Items Section */}
      {featuredItems.length > 0 && (
        <Card
          title={
            <Space>
              <StarOutlined style={{ color: '#faad14' }} />
              <span>Featured Items</span>
            </Space>
          }
          style={{ marginBottom: 24 }}
        >
          <Row gutter={[16, 16]}>
            {featuredItems.slice(0, 4).map((item) => (
              <Col xs={24} sm={12} md={6} key={item.id}>
                <MarketCard
                  item={item}
                  onClick={() => navigate(`/market/items/${item.id}`)}
                  onDownload={() => handleDownload(item)}
                />
              </Col>
            ))}
          </Row>
        </Card>
      )}

      {/* Search and Filters */}
      <Card>
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          {/* Search Bar */}
          <Input
            size="large"
            placeholder="Search for operators and packages..."
            prefix={<SearchOutlined />}
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            onPressEnter={handleSearch}
            allowClear
          />

          {/* Filters */}
          <Row gutter={16} align="middle">
            <Col xs={24} sm={6}>
              <Select
                placeholder="Type"
                value={filters.itemType}
                onChange={(value) => setFilters({ ...filters, itemType: value })}
                style={{ width: '100%' }}
                allowClear
              >
                <Select.Option value="OPERATOR">
                  <Space>
                    <CodeOutlined /> Operator
                  </Space>
                </Select.Option>
                <Select.Option value="PACKAGE">
                  <Space>
                    <AppstoreOutlined /> Package
                  </Space>
                </Select.Option>
              </Select>
            </Col>

            <Col xs={24} sm={6}>
              <Select
                placeholder="Category"
                value={filters.category}
                onChange={(value) => setFilters({ ...filters, category: value })}
                style={{ width: '100%' }}
                allowClear
              >
                <Select.Option value={1}>Data Processing</Select.Option>
                <Select.Option value={2}>File Operations</Select.Option>
                <Select.Option value={3}>Data Validation</Select.Option>
                <Select.Option value={4}>Data Transformation</Select.Option>
              </Select>
            </Col>

            <Col xs={24} sm={6}>
              <Select
                placeholder="Sort By"
                value={filters.sortBy}
                onChange={(value) => setFilters({ ...filters, sortBy: value })}
                style={{ width: '100%' }}
              >
                <Select.Option value="createdAt">Latest</Select.Option>
                <Select.Option value="averageRating">Top Rated</Select.Option>
                <Select.Option value="downloadsCount">Most Downloaded</Select.Option>
                <Select.Option value="viewsCount">Most Viewed</Select.Option>
              </Select>
            </Col>

            <Col xs={24} sm={6}>
              <Space>
                <Button type="primary" onClick={handleSearch}>
                  Search
                </Button>
                <Button onClick={handleReset}>
                  Reset
                </Button>
              </Space>
            </Col>
          </Row>

          {/* Results */}
          <div>
            <Space style={{ marginBottom: 16 }}>
              <span style={{ fontSize: '16px', fontWeight: 500 }}>
                {pagination.total} items found
              </span>
            </Space>

            {loading ? (
              <div style={{ textAlign: 'center', padding: '48px' }}>
                <Spin size="large" />
              </div>
            ) : items.length === 0 ? (
              <Empty
                description="No items found"
                style={{ padding: '48px' }}
              />
            ) : (
              <>
                <Row gutter={[16, 16]}>
                  {items.map((item) => (
                    <Col xs={24} sm={12} md={8} lg={6} key={item.id}>
                      <MarketCard
                        item={item}
                        onClick={() => navigate(`/market/items/${item.id}`)}
                        onDownload={() => handleDownload(item)}
                      />
                    </Col>
                  ))}
                </Row>

                {/* Pagination */}
                <div style={{ marginTop: 24, textAlign: 'center' }}>
                  <Space>
                    <Button
                      disabled={pagination.current === 1}
                      onClick={() =>
                        setPagination({ ...pagination, current: pagination.current - 1 })
                      }
                    >
                      Previous
                    </Button>
                    <span>
                      Page {pagination.current} of{' '}
                      {Math.ceil(pagination.total / pagination.pageSize)}
                    </span>
                    <Button
                      disabled={
                        pagination.current >=
                        Math.ceil(pagination.total / pagination.pageSize)
                      }
                      onClick={() =>
                        setPagination({ ...pagination, current: pagination.current + 1 })
                      }
                    >
                      Next
                    </Button>
                  </Space>
                </div>
              </>
            )}
          </div>
        </Space>
      </Card>
    </div>
  );
};

// Market Card Component
interface MarketCardProps {
  item: MarketItem;
  onClick: () => void;
  onDownload: () => void;
}

const MarketCard: React.FC<MarketCardProps> = ({ item, onClick, onDownload }) => {
  return (
    <Card
      hoverable
      className="market-card"
      cover={
        <div
          className="market-card-cover"
          onClick={onClick}
        >
          {item.itemType === 'OPERATOR' ? (
            <CodeOutlined style={{ fontSize: 48, color: '#1677ff' }} />
          ) : (
            <AppstoreOutlined style={{ fontSize: 48, color: '#52c41a' }} />
          )}
        </div>
      }
      actions={[
        <Button
          type="text"
          icon={<DownloadOutlined />}
          onClick={(e) => {
            e.stopPropagation();
            onDownload();
          }}
        >
          {item.downloadsCount || 0}
        </Button>,
      ]}
    >
      <Card.Meta
        title={
          <div className="market-card-title" onClick={onClick}>
            <Space direction="vertical" size={4}>
              <span>{item.name}</span>
              <Space size={4}>
                <Tag color={item.itemType === 'OPERATOR' ? 'blue' : 'green'}>
                  {item.itemType}
                </Tag>
                {item.operatorLanguage && (
                  <Tag color="geekblue">{item.operatorLanguage}</Tag>
                )}
                {item.featured && <Tag color="gold">Featured</Tag>}
              </Space>
            </Space>
          </div>
        }
        description={
          <div onClick={onClick}>
            <div className="market-card-description">
              {item.description || item.itemType === 'OPERATOR'
                ? item.operatorDescription
                : item.packageBusinessScenario}
            </div>
            <div className="market-card-footer">
              <Space size={16}>
                <Space size={4}>
                  <StarOutlined style={{ color: '#faad14' }} />
                  <span style={{ fontWeight: 500 }}>
                    {item.averageRating?.toFixed(1) || '0.0'}
                  </span>
                  <span style={{ color: '#8c8c8c' }}>
                    ({item.ratingsCount || 0})
                  </span>
                </Space>
                {item.tags?.slice(0, 2).map((tag) => (
                  <Tag key={tag} style={{ fontSize: '11px' }}>
                    {tag}
                  </Tag>
                ))}
              </Space>
            </div>
          </div>
        }
      />
    </Card>
  );
};

export default MarketListPage;
