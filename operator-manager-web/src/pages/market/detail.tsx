import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Button,
  Space,
  Tag,
  Descriptions,
  Rate,
  Input,
  message,
  List,
  Avatar,
  Modal,
  Tabs,
  Row,
  Col,
  Divider,
  Typography,
} from 'antd';
import {
  ArrowLeftOutlined,
  DownloadOutlined,
  StarOutlined,
  SendOutlined,
} from '@ant-design/icons';
import type { MarketItem, Review } from '@/types';
import { marketApi } from '@/api/market';
import './market.css';

const { TextArea } = Input;
const { TabPane } = Tabs;
const { Text, Paragraph } = Typography;

/**
 * Market item detail page
 */
const MarketDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [item, setItem] = useState<MarketItem | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [reviewModalVisible, setReviewModalVisible] = useState(false);
  const [userRating, setUserRating] = useState(0);
  const [userReview, setUserReview] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (id) {
      fetchItem();
      fetchReviews();
    }
  }, [id]);

  const fetchItem = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const response = await marketApi.getItem(Number(id));
      if (response.data) {
        setItem(response.data);
      }
    } catch (error: any) {
      message.error(error.message || 'Failed to fetch item');
    } finally {
      setLoading(false);
    }
  };

  const fetchReviews = async () => {
    if (!id) return;
    try {
      const response = await marketApi.getReviews(Number(id), 0, 20);
      if (response.data) {
        setReviews(response.data);
      }
    } catch (error: any) {
      console.error('Failed to fetch reviews:', error);
    }
  };

  const handleDownload = async () => {
    if (!item) return;
    try {
      await marketApi.downloadItem(item.id);
      message.success('Item downloaded successfully!');
      fetchItem(); // Refresh to update download count
    } catch (error: any) {
      message.error(error.message || 'Failed to download item');
    }
  };

  const handleRatingChange = async (value: number) => {
    if (!item) return;
    try {
      await marketApi.submitRating(item.id, { rating: value });
      message.success('Rating submitted!');
      fetchItem();
    } catch (error: any) {
      message.error(error.message || 'Failed to submit rating');
    }
  };

  const handleReviewSubmit = async () => {
    if (!item || userRating === 0) {
      message.warning('Please select a rating');
      return;
    }
    if (!userReview.trim()) {
      message.warning('Please write a review');
      return;
    }

    setSubmitting(true);
    try {
      await marketApi.submitReview(item.id, {
        content: userReview,
        rating: userRating,
      });
      message.success('Review submitted!');
      setReviewModalVisible(false);
      setUserReview('');
      setUserRating(0);
      fetchReviews();
      fetchItem();
    } catch (error: any) {
      message.error(error.message || 'Failed to submit review');
    } finally {
      setSubmitting(false);
    }
  };

  const handleLikeReview = async (reviewId: number) => {
    try {
      await marketApi.likeReview(reviewId);
      message.success('Review liked!');
      fetchReviews();
    } catch (error: any) {
      message.error(error.message || 'Failed to like review');
    }
  };

  if (!item) {
    return <div>Loading...</div>;
  }

  return (
    <div style={{ padding: '24px' }}>
      {/* Header */}
      <Card
        title={
          <Space>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={() => navigate('/market')}
            >
              Back to Market
            </Button>
          </Space>
        }
        extra={
          <Button
            type="primary"
            size="large"
            icon={<DownloadOutlined />}
            onClick={handleDownload}
          >
            Download ({item.downloadsCount || 0})
          </Button>
        }
        style={{ marginBottom: 24 }}
      >
        <Row gutter={24}>
          <Col span={16}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              {/* Title and Type */}
              <div>
                <Space size="middle" wrap>
                  <h1 style={{ margin: 0, fontSize: '28px' }}>{item.name}</h1>
                  <Tag color={item.itemType === 'OPERATOR' ? 'blue' : 'green'} style={{ fontSize: '14px' }}>
                    {item.itemType}
                  </Tag>
                  {item.operatorLanguage && (
                    <Tag color="geekblue" style={{ fontSize: '14px' }}>
                      {item.operatorLanguage}
                    </Tag>
                  )}
                  {item.featured && (
                    <Tag color="gold" style={{ fontSize: '14px' }}>
                      Featured
                    </Tag>
                  )}
                </Space>
              </div>

              {/* Rating */}
              <Space size="large">
                <Space>
                  <Rate
                    disabled
                    value={Math.round(item.averageRating || 0)}
                    style={{ fontSize: '20px' }}
                  />
                  <Text strong style={{ fontSize: '18px' }}>
                    {item.averageRating?.toFixed(1) || '0.0'}
                  </Text>
                  <Text type="secondary">({item.ratingsCount || 0} ratings)</Text>
                </Space>
                <Divider type="vertical" />
                <Space>
                  <Text type="secondary">{item.reviewsCount || 0} reviews</Text>
                </Space>
              </Space>

              {/* Description */}
              <div>
                <Text strong style={{ fontSize: '16px' }}>Description</Text>
                <Paragraph style={{ fontSize: '14px', marginTop: 8 }}>
                  {item.description || item.itemType === 'OPERATOR'
                    ? item.operatorDescription
                    : item.packageBusinessScenario}
                </Paragraph>
              </div>

              {/* Tags */}
              {item.tags && item.tags.length > 0 && (
                <div>
                  <Text strong>Tags:</Text>
                  <div style={{ marginTop: 8 }}>
                    <Space size="8" wrap>
                      {item.tags.map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                      ))}
                    </Space>
                  </div>
                </div>
              )}
            </Space>
          </Col>

          <Col span={8}>
            <Card size="small" title="Statistics">
              <Space direction="vertical" style={{ width: '100%' }} size="middle">
                <div>
                  <Text type="secondary">Downloads</Text>
                  <br />
                  <Text strong style={{ fontSize: '24px' }}>
                    {item.downloadsCount || 0}
                  </Text>
                </div>
                <div>
                  <Text type="secondary">Views</Text>
                  <br />
                  <Text strong style={{ fontSize: '24px' }}>
                    {item.viewsCount || 0}
                  </Text>
                </div>
                <div>
                  <Text type="secondary">Average Rating</Text>
                  <br />
                  <Text strong style={{ fontSize: '24px' }}>
                    {item.averageRating?.toFixed(1) || '0.0'}
                  </Text>
                </div>
                <Divider style={{ margin: '12px 0' }} />
                <div>
                  <Text strong>Rate this item:</Text>
                  <br />
                  <div style={{ marginTop: 8 }}>
                    <Rate
                      value={userRating}
                      onChange={handleRatingChange}
                      style={{ fontSize: '20px' }}
                    />
                  </div>
                </div>
                <Button
                  type="primary"
                  block
                  icon={<StarOutlined />}
                  onClick={() => setReviewModalVisible(true)}
                >
                  Write a Review
                </Button>
              </Space>
            </Card>
          </Col>
        </Row>
      </Card>

      {/* Reviews Section */}
      <Card title={`Reviews (${reviews.length})`}>
        <List
          dataSource={reviews}
          renderItem={(review) => (
            <List.Item key={review.id}>
              <Card className="review-card" style={{ width: '100%' }}>
                <div className="review-header">
                  <Space>
                    <Avatar size="small">{review.userName?.charAt(0).toUpperCase()}</Avatar>
                    <Text className="review-author">{review.userName}</Text>
                  </Space>
                  <Text className="review-date">
                    {new Date(review.createdAt).toLocaleDateString()}
                  </Text>
                </div>
                {review.rating && (
                  <div style={{ marginBottom: 8 }}>
                    <Rate disabled value={review.rating} style={{ fontSize: '14px' }} />
                  </div>
                )}
                <Paragraph className="review-content" style={{ marginBottom: 8 }}>
                  {review.content}
                </Paragraph>
                <div className="review-actions">
                  <Space size={24}>
                    <span className="review-action" onClick={() => handleLikeReview(review.id)}>
                      <Space size={4}>
                        <StarOutlined />
                        <span>{review.likesCount || 0} likes</span>
                      </Space>
                    </span>
                    {review.replies && review.replies.length > 0 && (
                      <span className="review-action">
                        {review.replies.length} replies
                      </span>
                    )}
                  </Space>
                </div>
              </Card>
            </List.Item>
          )}
          locale={{ emptyText: 'No reviews yet. Be the first to review!' }}
        />
      </Card>

      {/* Review Modal */}
      <Modal
        title="Write a Review"
        open={reviewModalVisible}
        onOk={handleReviewSubmit}
        onCancel={() => {
          setReviewModalVisible(false);
          setUserReview('');
          setUserRating(0);
        }}
        confirmLoading={submitting}
        width={600}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <div>
            <Text strong>Your Rating</Text>
            <div style={{ marginTop: 8 }}>
              <Rate
                value={userRating}
                onChange={setUserRating}
                style={{ fontSize: '24px' }}
              />
            </div>
          </div>
          <div>
            <Text strong>Your Review</Text>
            <TextArea
              rows={6}
              value={userReview}
              onChange={(e) => setUserReview(e.target.value)}
              placeholder="Share your experience with this item..."
              style={{ marginTop: 8 }}
            />
          </div>
        </Space>
      </Modal>
    </div>
  );
};

export default MarketDetailPage;
